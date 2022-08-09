package gov.nih.nci.bento.service;

import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service("redisService")
public class RedisService {
    private static final Logger logger = LogManager.getLogger(RedisService.class);

    @Autowired
    private ConfigurationDAO config;

    private JedisPool pool;
    private JedisCluster cluster;
    private Boolean useCluster;
    private int ttl;
    private boolean isInitialized;
    private boolean groupListsInitialized;
    private HashSet<String> groups = new HashSet<>();
    private HashMap<String, String> parameterMappings = new HashMap<>();

    @PostConstruct
    public void init() {
        isInitialized = connect();
    }

    @PreDestroy
    private void close() {
        if (null != pool) {
            pool.close();
        }
        if (null != cluster) {
            cluster.close();
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public String[] getGroups() {
        return formatKeys(groups.toArray(new String[0]));
    }

    public void cacheGroup(String key, String[] value) {
        cacheValue(key, value, true);
        groups.add(key.split(":")[0]);
    }

    public void cacheValue(String key, String value) {
        cacheValue(key, new String[]{value}, false);
    }

    public void cacheValue(String keyInput, String[] values, boolean isSet) {
        String key = formatKey(keyInput);
        Jedis jedis = null;
        try {
            if (isSet) {
                key = addSetHashTag(key);
                if (useCluster) {
                    cluster.sadd(key, values);
                } else {
                    jedis = pool.getResource();
                    jedis.sadd(key, values);
                }
            } else {
                if (useCluster) {
                    if (ttl > 0) {
                        cluster.setex(key, config.getRedisTTL(), values[0]);
                    } else {
                        cluster.set(key, values[0]);
                    }
                } else {
                    jedis = pool.getResource();
                    if (ttl > 0) {
                        jedis.setex(key, config.getRedisTTL(), values[0]);
                    } else {
                        jedis.set(key, values[0]);
                    }
                }
            }
            logger.info("Cache Entry Created");
        } catch (NullPointerException e) {
            logger.warn("Redis not connected, query won't be cached!");
        } catch (JedisException e) {
            logger.error(e);
            logger.warn("Redis exception caught, query won't be cached!");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String getCachedValue(String key) {
        try {
            return getFromCache(new String[]{key}, RETURNTYPE.VALUE).iterator().next();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Set<String> getCachedSet(String key) {
        return getFromCache(new String[]{key}, RETURNTYPE.SET);
    }

    public Set<String> getUnion(String[] keys) {
        return getFromCache(keys, RETURNTYPE.UNION);
    }

    public Set<String> getIntersection(String[] keys) {
        return getFromCache(keys, RETURNTYPE.INTERSECTION);
    }

    public Long unionStore(String newKey, String[] keys) {
        Long output = store(keys, newKey, STORETYPE.UNION);
        if (output > 0) {
            logger.info("Union stored: " + newKey);
        }
        return output;
    }

    public Long interStore(String newKey, String[] keys) {
        Long output = store(keys, newKey, STORETYPE.INTERSECTION);
        if (output > 0) {
            logger.info("Intersection stored: " + newKey);
        }
        return output;
    }

    public boolean isGroupListsInitialized() {
        return groupListsInitialized;
    }

    public void setGroupListsInitialized(boolean groupListsInitialized) {
        this.groupListsInitialized = groupListsInitialized;
    }

    public HashMap<String, String> getParameterMappings() {
        return parameterMappings;
    }

    public void setParameterMappings(HashMap<String, String> parameterMappings) {
        this.parameterMappings = parameterMappings;
    }

    private Set<String> getFromCache(String[] keysInput, RETURNTYPE operation) {
        String[] keys = formatKeys(keysInput);
        Jedis jedis = null;
        String key = keys[0];
        String taggedKey = addSetHashTag(key);
        String[] taggedKeys = addSetHashTag(keys);
        try {
            switch (operation) {
                case VALUE:
                    if (useCluster) {
                        String value = cluster.get(key);
                        Set<String> output = new HashSet<>();
                        output.add(value);
                        return output;
                    } else {
                        jedis = pool.getResource();
                        Set<String> output = new HashSet<>();
                        output.add(jedis.get(key));
                        return output;
                    }
                case SET:
                    if (useCluster) {
                        return cluster.smembers(taggedKey);
                    } else {
                        jedis = pool.getResource();
                        return jedis.smembers(taggedKey);
                    }
                case UNION:
                    if (useCluster) {
                        return cluster.sunion(taggedKeys);
                    } else {
                        jedis = pool.getResource();
                        return jedis.sunion(taggedKeys);
                    }
                case INTERSECTION:
                    if (useCluster) {
                        return cluster.sinter(taggedKeys);
                    } else {
                        jedis = pool.getResource();
                        return jedis.sinter(taggedKeys);
                    }
                default:
                    logger.error("Invalid RETURN_TYPE parameter, fall back to query neo4j");
                    return null;
            }
        } catch (NullPointerException e) {
            logger.warn("Redis not connected, fall back to query Neo4j!");
            return null;
        } catch (JedisException e) {
            logger.error(e);
            logger.warn("Redis exception caught, fall back to query Neo4j!");
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private Long store(String[] keysInput, String newKeyInput, STORETYPE type) {
        //Format keys
        String[] keys = addSetHashTag(formatKeys(keysInput));
        String newKey = addSetHashTag(formatKey(newKeyInput));
        Jedis jedis = null;
        try {
            if (type == STORETYPE.UNION) {
                if (useCluster) {
                    return cluster.sunionstore(newKey, keys);
                } else {
                    jedis = pool.getResource();
                    return jedis.sunionstore(newKey, keys);
                }
            } else {
                if (useCluster) {
                    return cluster.sinterstore(newKey, keys);
                } else {
                    jedis = pool.getResource();
                    return jedis.sinterstore(newKey, keys);
                }
            }
        } catch (NullPointerException e) {
            logger.warn("Redis not connected, query won't be cached!");
        } catch (JedisException e) {
            logger.error(e);
            logger.warn("Redis exception caught, query won't be cached!");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0L;
    }

    private boolean connect() {
        if (!config.isRedisEnabled()) {
            logger.warn("Redis not connected, connection disabled in Bento configuration");
            return false;
        }
        try {
            String host = config.getRedisHost();
            int port = config.getRedisPort();
            useCluster = config.isRedisUseCluster();
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

    private String[] formatKeys(String[] keys) {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = formatKey(keys[i]);
        }
        return keys;
    }

    private String formatKey(String keyInput) {
        String key = keyInput.replaceAll("\"", "");
        key = key.replaceAll(" ", "_");
        return key;
    }

    private String[] addSetHashTag(String[] keys){
        String[] taggedKeys = Arrays.copyOf(keys, keys.length);
        for(int i = 0; i < taggedKeys.length; i++){
            taggedKeys[i] = addSetHashTag(taggedKeys[i]);
        }
        return taggedKeys;
    }

    private String addSetHashTag(String key){
        return "{set}."+key;
    }

    private enum RETURNTYPE {
        VALUE,
        SET,
        UNION,
        INTERSECTION
    }

    private enum STORETYPE {
        UNION,
        INTERSECTION
    }
}
