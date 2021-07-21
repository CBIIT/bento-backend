package gov.nih.nci.bento.service;

import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    public String query() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termsQuery("endo_therapy", "AI"));
        sourceBuilder.query(QueryBuilders.termsQuery("diagnosis", "Paget's Disease"));
        sourceBuilder.query(QueryBuilders.termsQuery("tumor_size", "<=1" , "(2,3]"));
        sourceBuilder.from(0);
        sourceBuilder.size(20);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("dashboard"); // Hardcoded index name
        searchRequest.source(sourceBuilder);
        return "hello";

    }
}