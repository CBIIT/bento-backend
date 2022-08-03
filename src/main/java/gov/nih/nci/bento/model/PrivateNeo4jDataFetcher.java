package gov.nih.nci.bento.model;

import org.neo4j.graphql.DataFetchingInterceptor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service("privateNeo4jDataFetcher")
@DependsOn({"redisService"})
public class PrivateNeo4jDataFetcher extends AbstractNeo4jDataFetcher implements AutoCloseable, DataFetchingInterceptor {
}

