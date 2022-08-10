package gov.nih.nci.bento.model;

import gov.nih.nci.bento.service.RedisService;
import graphql.language.VariableReference;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.graphql.Cypher;
import org.neo4j.graphql.DataFetchingInterceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractNeo4jDataFetcher implements AutoCloseable, DataFetchingInterceptor {
    private static final Logger logger = LogManager.getLogger(AbstractNeo4jDataFetcher.class);

    private int cacheHits = 0;
    private int cacheMisses = 0;

    private Driver driver;

    private final ConfigurationDAO config;
    private final RedisService redisService;

    protected AbstractNeo4jDataFetcher(ConfigurationDAO config, RedisService redisService) {
        this.config = config;
        this.redisService = redisService;
        connect();
    }

    private void connect() {
        String uri = config.getNeo4jUrl();
        String user = config.getNeo4jUser();
        String password = config.getNeo4jPassword();
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    @Nullable
    @Override
    public Object fetchData(@NotNull DataFetchingEnvironment dataFetchingEnvironment, @NotNull DataFetcher<Cypher> dataFetcher) throws Exception {
        try (Session session = driver.session()) {
            Cypher cypher = dataFetcher.get(dataFetchingEnvironment);
            Map<String, Object> params = cypher.getParams();
            Object values = null;
            Map<String, Object> transformedParams = transformParams(params, dataFetchingEnvironment.getVariables());

            if (redisService.isInitialized()) {
                String redisKey = cypher.getVariable() + "::" + transformedParams;
                String cachedValue = redisService.getCachedValue(redisKey);
                values = deserializeObject(cachedValue);
                if (values == null) {
                    values = executeQuery(session, cypher, transformedParams);
                    redisService.cacheValue(redisKey, serializeObject(values));
                    logger.info("Cache Miss: Query executed and cache entry created");
                    cacheMisses++;
                }
                else{
                    logger.info("Cache Hit: Cached response retrieved");
                    cacheHits++;
                }
                int ratio = (int) ((double)cacheHits/(double)(cacheHits+cacheMisses)*100);
                logger.info(String.format("Cache Hit-Miss Ratio: %s-%s, %s%%", cacheHits, cacheMisses, ratio));
                return values;
            } else {
                logger.info("Cache Disabled: Executing query");
                return executeQuery(session, cypher, transformedParams);
            }
        }
    }

    private Object executeQuery(Session session, Cypher cypher, Map<String, Object> variables){
        Result result = session.run(cypher.getQuery(), variables);
        String key = result.keys().get(0);
        Object values = null;
        if (isList(cypher.getType())) {
            List<Object> list = new ArrayList<>();
            values = list;
            while (result.hasNext()) {
                Record rec = result.next();
                list.add(rec.get(key).asObject());
            }
        } else {
            if (result.hasNext()) {
                Record rec = result.next();
                values = rec.get(key).asObject();
            }
        }
        return values;
    }

    private String serializeObject(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private Object deserializeObject(String s) throws IOException, ClassNotFoundException {
        try{
            byte[] data = Base64.getDecoder().decode(s);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
        }
        catch (Exception e){
            return null;
        }

    }

    private Map<String, Object> transformParams(Map<String, Object> param, Map<String, Object> variables) {
        Map<String, Object> result = new HashMap<>();
        for (String key : param.keySet()) {
            Object value = param.get(key);
            result.put(key, mapParam(value, variables));
        }
        return result;
    }

    private Object mapParam(Object param, Map<String, Object> variables) {
        if (param instanceof List) {
            List<Object> result = new ArrayList<>();
            for (Object val: (List<Object>)param) {
                result.add(mapParam(val, variables));
            }
            return result;
        } else if (param.getClass() == VariableReference.class) {
            return variables.get(((VariableReference) param).getName());
        } else if (param.getClass() == BigInteger.class) {
            return ((BigInteger) param).longValueExact();
        } else if (param.getClass() == BigDecimal.class) {
            return ((BigDecimal) param).doubleValue();
        } else {
            return param;
        }
    }

    private boolean isList(GraphQLType type) {
        if (type instanceof GraphQLList) {
            return true;
        } else if (type instanceof GraphQLNonNull) {
            return isList(((GraphQLNonNull) type).getWrappedType());
        } else {
            return false;
        }
    }
}

