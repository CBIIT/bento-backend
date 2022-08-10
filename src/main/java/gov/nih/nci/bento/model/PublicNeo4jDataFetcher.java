package gov.nih.nci.bento.model;

import gov.nih.nci.bento.service.RedisService;

public class PublicNeo4jDataFetcher extends AbstractNeo4jDataFetcher{
    public PublicNeo4jDataFetcher(ConfigurationDAO config, RedisService redisService) {
        super(config, redisService);
    }
}

