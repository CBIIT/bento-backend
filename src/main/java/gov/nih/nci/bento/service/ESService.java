package gov.nih.nci.bento.service;

import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Service("ESService")
public class ESService {
    private static final Logger logger = LogManager.getLogger(RedisService.class);

    @Autowired
    private ConfigurationDAO config;

    private RestHighLevelClient client;

    @PostConstruct
    public void init() {
        logger.info("Initializing Elasticsearch client");
        var lowLevelBuilder = RestClient.builder(new HttpHost(config.getEsHost(), 9200, "http"));
        client = new RestHighLevelClient( lowLevelBuilder );
    }

    @PreDestroy
    private void close() throws IOException {
        client.close();
    }

    public SearchResponse query(SearchRequest request) throws IOException{
        return client.search(request, RequestOptions.DEFAULT);
    }
}