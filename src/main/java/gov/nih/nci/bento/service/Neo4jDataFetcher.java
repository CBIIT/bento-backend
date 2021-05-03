package gov.nih.nci.bento.service;

import gov.nih.nci.bento.model.ConfigurationDAO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

@Service("neo4jDataFetcher")
@DependsOn({"redisService"})
public class Neo4jDataFetcher implements AutoCloseable, DataFetchingInterceptor {
    private static final Logger logger = LogManager.getLogger(Neo4jDataFetcher.class);

    private Driver driver;
    @Autowired
    private ConfigurationDAO config;
    @Autowired
    private RedisService redisService;

    @PostConstruct
    public void connect() {
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
                String redisKey = cypher.getQuery() + "::" + transformedParams;
                String cachedValue = redisService.getCachedValue(redisKey);
                values = deserializeObject(cachedValue);
                if (values == null) {
                    values = executeQuery(session, cypher, transformedParams);
                    redisService.cacheValue(redisKey, serializeObject(values));
                }
                return values;
            } else {
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
            if (value.getClass() == VariableReference.class) {
                result.put(key, variables.get(((VariableReference) value).getName()));
            } else if (value.getClass() == BigInteger.class) {
                result.put(key, ((BigInteger) value).longValueExact());
            } else if (value.getClass() == BigDecimal.class) {
                result.put(key, ((BigDecimal) value).doubleValue());
            } else {
                result.put(key, value);
            }
        }
        return result;
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

