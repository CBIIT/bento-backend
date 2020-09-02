package gov.nih.nci.bento.service;

import gov.nih.nci.bento.controller.GraphQLController;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisException;

import javax.annotation.PostConstruct;

@Service
public class RedisService {
    private static final Logger logger = LogManager.getLogger(GraphQLController.class);

    @Autowired
    private ConfigurationDAO config;
    private JedisPool pool;

    @PostConstruct
    public boolean connect() {
        try {
            String host = config.getRedisHost();
            String password = config.getRedisPassword();
            if (host.isBlank() || password.isBlank()) {
               return false;
            } else {
                pool = new JedisPool(getJedisConfig(), host, Protocol.DEFAULT_PORT, Protocol.DEFAULT_TIMEOUT, password);
                return true;
            }
        } catch (JedisException e) {
            logger.error(e);
            pool.close();
            pool = null;
            return false;
        }
    }

    private JedisPoolConfig getJedisConfig() {
        return new JedisPoolConfig();
    }

    public void finalize() {
        if (null != pool) {
            pool.close();
        }
    }

    public String getQueryResult(String query) {
        if (null == pool) {
            logger.warn("Redis not connected, fall back to query Neo4j!");
            return null;
        }

        try (Jedis jedis = pool.getResource())  {
            return jedis.get(query);
        } catch (JedisException e) {
            logger.error(e);
            logger.warn("Redis exception caught, fall back to query Neo4j!");
            return null;
        }
    }

    public boolean setQueryResult(String query, String result) {
        if (null == pool) {
            logger.warn("Redis not connected, query won't be cached!");
            return false;
        }
        try (Jedis jedis = pool.getResource())  {
            String status = jedis.setex(query, config.getRedisTTL(), result);
            return status.equals("OK");
        } catch (JedisException e) {
            logger.error(e);
            logger.warn("Redis exception caught, query won't be cached!");
            return false;
        }
    }
}
