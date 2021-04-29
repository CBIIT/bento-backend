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
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Neo4jDataFetcher implements AutoCloseable, DataFetchingInterceptor {
    private static final Logger logger = LogManager.getLogger(Neo4jDataFetcher.class);

    private Driver driver;
    @Autowired
    private ConfigurationDAO config;

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
            Result result = session.run(cypher.getQuery(), transformParams(params, dataFetchingEnvironment.getVariables()));
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

