package gov.nih.nci.bento.model;

import gov.nih.nci.bento.service.RedisService;

public class PrivateNeo4jDataFetcher extends AbstractNeo4jDataFetcher{
    public PrivateNeo4jDataFetcher(ConfigurationDAO config, RedisService redisService) {
        super(config, redisService);
    }
}
