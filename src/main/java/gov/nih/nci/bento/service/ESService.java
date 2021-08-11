package gov.nih.nci.bento.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.*;
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

    private RestClient client;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @PostConstruct
    public void init() {
        logger.info("Initializing Elasticsearch client");
        var lowLevelBuilder = RestClient.builder(new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme()));
        client = lowLevelBuilder.build();
    }

    @PreDestroy
    private void close() throws IOException {
        client.close();
    }

    public Response send(Request request) throws IOException{
        return client.performRequest(request);
    }

    public List<String> collectAll(Request request, String fieldName) throws IOException {
        List<String> results = new ArrayList<>();

        request.addParameter("scroll", "10S");
        Response response = send(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            // Todo: should add appropriate error messages, and return error also
            logger.error("Elasticsearch returned code: " + statusCode);
            return null;
        }
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");

        while (searchHits != null && searchHits.size() > 0) {
            for (int i = 0; i < searchHits.size(); i++) {
                String value = searchHits.get(i).getAsJsonObject().get("_source").getAsJsonObject().get(fieldName).getAsString();
                results.add(value);
            }

            Request scrollRequest = new Request("POST", "/_search/scroll");
            String scrollId = jsonObject.get("_scroll_id").getAsString();
            String body = "{\"scroll\":\"10S\",\"scroll_id\":\"" + scrollId + "\"}";
            scrollRequest.setJsonEntity(body);
            response = send(scrollRequest);
            responseBody = EntityUtils.toString(response.getEntity());
            jsonObject = gson.fromJson(responseBody, JsonObject.class);
            searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        }

        String scrollId = jsonObject.get("_scroll_id").getAsString();
        Request clearScrollRequest = new Request("DELETE", "/_search/scroll");
        clearScrollRequest.setJsonEntity("{\"scroll_id\":\"" + scrollId +"\"}");
        response = send(clearScrollRequest);
        statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            logger.error("Elasticsearch returned code: " + statusCode + " when cleaning up scrolls");
        }

        return results;
    }
}