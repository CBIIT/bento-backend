package gov.nih.nci.bento.service;

import gov.nih.nci.bento.controller.GraphQLController;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import javax.annotation.PostConstruct;

@Service
public class RedisService {
    private static final Logger logger = LogManager.getLogger(GraphQLController.class);

    @Autowired
    private ConfigurationDAO config;
    private Jedis jedis;

    @PostConstruct
    public boolean connect() {
        try {
            String host = config.getRedisHost();
            String password = config.getRedisPassword();
            if (host.isBlank() || password.isBlank()) {
               return false;
            } else {
                jedis = new Jedis(host);
                String result = jedis.auth(password);
                return result.equals("OK");
            }
        } catch (JedisException e) {
            logger.error(e);
            jedis.disconnect();
            jedis = null;
            return false;
        }
    }

    public String getQueryResult(String query) {
        try {
            if (jedis != null) {
                return jedis.get(query);
            } else {
                logger.warn("Redis not connected, fall back to query Neo4j!");
                return null;
            }
        } catch (JedisException e) {
            logger.error(e);
            logger.warn("Redis exception caught, fall back to query Neo4j!");
            return null;
        }
    }

    public boolean setQueryResult(String query, String result) {
        try {
            if (jedis != null) {
                String status = jedis.setex(query, config.getRedisTTL(), result);
                return status.equals("OK");
            } else {
                logger.warn("Redis not connected, query won't be cached!");
                return false;
            }
        } catch (JedisException e) {
            logger.error(e);
            logger.warn("Redis exception caught, query won't be cached!");
            return false;
        }
    }
}
