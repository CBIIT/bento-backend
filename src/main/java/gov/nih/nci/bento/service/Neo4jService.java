package gov.nih.nci.bento.service;

import gov.nih.nci.bento.controller.GraphQLController;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static org.neo4j.driver.Values.parameters;

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

    public String query(final String cypherQuery)
    {
        try ( Session session = driver.session() )
        {
            String result = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run(cypherQuery,
                            parameters( "message", message ) );
                    return result.single().get( 0 ).asString();
                }
            } );
            return result;
        } catch (Exception e) {
            logger.error(e);
        }
    }
}

