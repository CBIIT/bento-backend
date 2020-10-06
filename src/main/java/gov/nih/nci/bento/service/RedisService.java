package gov.nih.nci.bento.service;

import gov.nih.nci.bento.controller.GraphQLController;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import javax.annotation.PostConstruct;

@Service
public class RedisService {
    private static final Logger logger = LogManager.getLogger(GraphQLController.class);

    @Autowired
    private ConfigurationDAO config;
    private JedisPool pool;
    private JedisCluster cluster;
    private Boolean useCluster;
    private int ttl;

    @PostConstruct
    public boolean connect() {
        try {
            String host = config.getRedisHost();
            int port = config.getRedisPort();
            useCluster = config.getRedisUseCluster();
            ttl = config.getRedisTTL();

            if (host.isBlank()) {
               return false;
            }

            if (useCluster) {
                cluster = new JedisCluster(new HostAndPort(host, port));
            } else {
                pool = new JedisPool(host, port);
            }
            return true;
        } catch (JedisException e) {
            logger.error(e);
            if (null != pool) {
                pool.close();
                pool = null;
            }
            if (null != cluster) {
                cluster.close();
                cluster = null;
            }
            return false;
        }
    }

    public void finalize() {
        if (null != pool) {
            pool.close();
        }
        if (null != cluster) {
            cluster.close();
        }
    }

    public String getQueryResult(String query) {
        if (useCluster) {
            if (null == cluster) {
                logger.warn("Redis not connected, fall back to query Neo4j!");
                return null;
            }
            try {
                return cluster.get(query);
            } catch (JedisException e) {
                logger.error(e);
                logger.warn("Redis exception caught, fall back to query Neo4j!");
                return null;
            }
        } else {
            if (null == pool) {
                logger.warn("Redis not connected, fall back to query Neo4j!");
                return null;
            }

            try (Jedis jedis = pool.getResource()) {
                return jedis.get(query);
            } catch (JedisException e) {
                logger.error(e);
                logger.warn("Redis exception caught, fall back to query Neo4j!");
                return null;
            }
        }
    }


    public boolean setQueryResult(String query, String result) {
        if (useCluster) {
            if (null == cluster) {
                logger.warn("Redis not connected, query won't be cached!");
                return false;
            }
            try {
                String status;
                if (ttl > 0) {
                    status = cluster.setex(query, config.getRedisTTL(), result);
                } else {
                    status = cluster.set(query, result);
                }
                return status.equals("OK");
            } catch (JedisException e) {
                logger.error(e);
                logger.warn("Redis exception caught, query won't be cached!");
                return false;
            }

        } else {
            if (null == pool) {
                logger.warn("Redis not connected, query won't be cached!");
                return false;
            }
            try (Jedis jedis = pool.getResource()) {
                String status;
                if (ttl > 0) {
                    status = jedis.setex(query, config.getRedisTTL(), result);
                } else {
                    status = jedis.set(query, result);
                }
                return status.equals("OK");
            } catch (JedisException e) {
                logger.error(e);
                logger.warn("Redis exception caught, query won't be cached!");
                return false;
            }
        }
    }
}
