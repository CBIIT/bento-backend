package gov.nih.nci.bento.service;

import gov.nih.nci.bento.model.ConfigurationDAO;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.*;
import org.neo4j.graphql.Cypher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Service
public class Neo4jService implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(Neo4jService.class);

    private Driver driver;
    @Autowired
    private ConfigurationDAO config;

    @PostConstruct
    public void connect()
    {
        String uri = config.getNeo4jUrl();
        String user = config.getNeo4jUser();
        String password = config.getNeo4jPassword();
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public GraphQLResult query(final Cypher cypher)
    {
        try ( Session session = driver.session() )
        {
            Result result = session.run(cypher.getQuery(), cypher.getParams());
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
            return new GraphQLResult(key, values);
        }
   }

   private boolean isList(GraphQLType type) {
        if (type instanceof GraphQLList) {
            return true;
        } else if (type instanceof GraphQLNonNull) {
            return isList(((GraphQLNonNull)type).getWrappedType());
        } else {
            return false;
        }
   }
}

