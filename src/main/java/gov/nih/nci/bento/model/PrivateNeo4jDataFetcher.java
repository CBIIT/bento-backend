package gov.nih.nci.bento.model;

import gov.nih.nci.bento.service.RedisService;
import org.neo4j.graphql.DataFetchingInterceptor;

public class PrivateNeo4jDataFetcher extends AbstractNeo4jDataFetcher implements AutoCloseable, DataFetchingInterceptor {
    public PrivateNeo4jDataFetcher(ConfigurationDAO config, RedisService redisService) {
        super(config, redisService);
    }
}
