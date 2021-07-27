package gov.nih.nci.bento.service;

import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public List<String> collectAll(SearchResponse response, Scroll scroll, String fieldName) throws IOException {
        List<String> results = new ArrayList<>();


        String scrollId = response.getScrollId();
        SearchHit[] searchHits = response.getHits().getHits();

        while (searchHits != null && searchHits.length > 0) {
            for (var hit: searchHits) {
                results.add((String)hit.getSourceAsMap().get(fieldName));
            }

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            response = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            searchHits = response.getHits().getHits();
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
        return results;
    }

}