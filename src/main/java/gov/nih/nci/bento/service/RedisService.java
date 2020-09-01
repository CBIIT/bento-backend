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
            jedis = new Jedis(config.getRedisHost());
            String result = jedis.auth(config.getRedisPassword());
            return result.equals("OK");
        } catch (JedisDataException authFailed) {
            logger.error(authFailed);
            System.exit(1);
            return false;
        } catch (JedisConnectionException connectionFailed) {
            logger.error(connectionFailed);
            System.exit(1);
            return false;
        }
    }

    public String getQueryResult(String query) {
        if (jedis != null) {
            return jedis.get(query);
        } else {
            logger.error("Redis not connected!");
            return null;
        }
    }

    public boolean setQueryResult(String query, String result) {
        if (jedis != null) {
            String status = jedis.setex(query, config.getRedisTTL(), result);
            return status.equals("OK");
        } else {
            logger.error("Redis not connected!");
            return false;
        }
    }
}
