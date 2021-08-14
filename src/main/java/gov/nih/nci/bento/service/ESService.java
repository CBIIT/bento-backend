package gov.nih.nci.bento.service;

import com.google.gson.*;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.*;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Service("ESService")
public class ESService {
    public final String JSON_OBJECT = "jsonObject";
    public final String AGGS = "aggs";

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
        Response response = client.performRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            String msg = "Elasticsearch returned code: " + statusCode;
            logger.error(msg);
            throw new IOException(msg);
        }
        return response;
    }

    public JsonObject getJSonFromResponse(Response response) throws IOException {
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        return jsonObject;
    }

    public Map<String, Object> buildFilterQuery(Map<String, Object> params, Set<String> excludedParams) {
        Map<String, Object> result = new HashMap<>();

        List<Object> filter = new ArrayList<>();
        for (var key: params.keySet()) {
            if (excludedParams.contains(key)) {
                continue;
            }
            List<String> valueSet = (List<String>) params.get(key);
            if (valueSet.size() > 0) {
                // list with only one empty string [""] means return all records
                if (valueSet.size() == 1) {
                    if (valueSet.get(0).equals("")) {
                        continue;
                    }
                }
                filter.add(Map.of(
                        "terms", Map.of( key, valueSet)
                ));
            }
        }

        result.put("query", Map.of("bool", Map.of("filter", filter)));
        return result;
    }

    public void addAggregations(Map<String, Object> query, String[] aggNames) {
        query.put("size", 0);
        query.put("aggregations", getAllAggregations(aggNames));
    }

    public void addSubAggregations(Map<String, Object> query, String mainAggName, String[] subAggNames) {
        Map<String, Object> mainAgg = (Map<String, Object>) ((Map<String, Object>) query.get("aggregations")).get(mainAggName);
        Map<String, Object> subAggs = getAllAggregations(subAggNames);
        mainAgg.put("aggregations", subAggs);
    }

    private Map<String, Object> getAllAggregations(String[]  aggNames) {
        Map<String, Object> aggs = new HashMap<>();
        for (String aggName: aggNames) {
            aggs.put(aggName, getSingleAggregation(aggName));
        }
        return aggs;
    }

    private Map<String, Object> getSingleAggregation(String aggName) {
        Map<String, Object> agg = new HashMap<>();
        agg.put("terms", Map.of("field", aggName));
        return agg;
    }

    public Map<String, Object> collectAggs(Response response, String[] aggNames) throws IOException{
        Map<String, Object> result = new HashMap<>();
        Map<String, JsonArray> data = new HashMap<>();
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        result.put(JSON_OBJECT, jsonObject);
        JsonObject aggs = jsonObject.getAsJsonObject("aggregations");
        for (String aggName: aggNames) {
            data.put(aggName, aggs.getAsJsonObject(aggName).getAsJsonArray("buckets"));
        }
        result.put(AGGS, data);
        return result;
    }



    public List<String> collectField(Request request, String fieldName) throws IOException {
        List<String> results = new ArrayList<>();

        request.addParameter("scroll", "10S");
        Response response = send(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");

        while (searchHits != null && searchHits.size() > 0) {
            logger.info("Current " + fieldName + " records: " + results.size() + " collecting...");
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
        send(clearScrollRequest);

        return results;
    }

    public List<Map<String, Object>> collectPage(JsonObject jsonObject, String[][] properties, int pageSize) throws IOException {
        return collectPage(jsonObject, properties, pageSize, 0);

    }

    public List<Map<String, Object>> collectPage(JsonObject jsonObject, String[][] properties, int pageSize, int offset) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();

        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        int size = Math.min(searchHits.size(), pageSize);
        for (int i = 0; i < size; i++) {
            Map<String, Object> row = new HashMap<>();
            for (String[] prop: properties) {
                String propName = prop[0];
                String dataField = prop[1];
                JsonElement element = searchHits.get(i).getAsJsonObject().get("_source").getAsJsonObject().get(dataField);
                row.put(propName, getValue(element));
            }
            data.add(row);
        }

        return data;
    }

    // Convert JsonElement into Java collections and primitives
    private Object getValue(JsonElement element) {
        Object value = null;
        if (element == null) {
            return null;
        } else if (element.isJsonObject()) {
            value = new HashMap<String, Object>();
            JsonObject object = element.getAsJsonObject();
            for (String key: object.keySet()) {
                ((Map<String, Object>) value).put(key, getValue(object.get(key)));
            }
        } else if (element.isJsonArray()) {
            value = new ArrayList<>();
            for (JsonElement entry: element.getAsJsonArray()) {
                ((List<Object>)value).add(getValue(entry));
            }
        } else {
            value = element.getAsString();
        }
        return value;
    }
}