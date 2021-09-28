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
    public static final String SCROLL_ENDPOINT = "/_search/scroll";
    public static final String JSON_OBJECT = "jsonObject";
    public static final String AGGS = "aggs";
    public static final int MAX_ES_SIZE = 10000;

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

    public JsonObject send(Request request) throws IOException{
        Response response = client.performRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            String msg = "Elasticsearch returned code: " + statusCode;
            logger.error(msg);
            throw new IOException(msg);
        }
        return getJSonFromResponse(response);
    }

    public JsonObject getJSonFromResponse(Response response) throws IOException {
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        return jsonObject;
    }

    // This function build queries with following rules:
    //  - If a list is empty, query will return empty dataset
    //  - If a list has only one element which is empty string, query will return all data available
    //  - If a list is null, query will return all data available
    public Map<String, Object> buildListQuery(Map<String, Object> params, Set<String> excludedParams) {
        Map<String, Object> result = new HashMap<>();

        List<Object> filter = new ArrayList<>();
        for (var key: params.keySet()) {
            if (excludedParams.contains(key)) {
                continue;
            }
            List<String> valueSet = (List<String>) params.get(key);
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

        result.put("query", Map.of("bool", Map.of("filter", filter)));
        return result;
    }

    public Map<String, Object> buildFacetFilterQuery(Map<String, Object> params) {
        return buildFacetFilterQuery(params, Set.of());
    }

    public Map<String, Object> buildFacetFilterQuery(Map<String, Object> params, Set<String> excludedParams) {
        Map<String, Object> result = new HashMap<>();

        List<Object> filter = new ArrayList<>();
        for (var key: params.keySet()) {
            if (excludedParams.contains(key)) {
                continue;
            }
            List<String> valueSet = (List<String>) params.get(key);
            if (valueSet.size() > 0) {
                filter.add(Map.of(
                        "terms", Map.of( key, valueSet)
                ));
            }
        }

        result.put("query", Map.of("bool", Map.of("filter", filter)));
        return result;
    }

    public Map<String, Object> addAggregations(Map<String, Object> query, String[] aggNames) {
        Map<String, Object> newQuery = new HashMap<>(query);
        newQuery.put("size", 0);
        newQuery.put("aggregations", getAllAggregations(aggNames));
        return newQuery;
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
        agg.put("terms", Map.of("field", aggName, "size", MAX_ES_SIZE));
        return agg;
    }

    public Map<String, JsonArray> collectAggs(JsonObject jsonObject, String[] aggNames) throws IOException{
        Map<String, JsonArray> data = new HashMap<>();
        JsonObject aggs = jsonObject.getAsJsonObject("aggregations");
        for (String aggName: aggNames) {
            data.put(aggName, aggs.getAsJsonObject(aggName).getAsJsonArray("buckets"));
        }
        return data;
    }

    public List<String> collectBucketKeys(JsonArray buckets) {
        List<String> keys = new ArrayList<>();
        for (var bucket: buckets) {
            keys.add(bucket.getAsJsonObject().get("key").getAsString());
        }
        return keys;
    }

    public List<String> collectField(Request request, String fieldName) throws IOException {
        List<String> results = new ArrayList<>();

        request.addParameter("scroll", "10S");
        JsonObject jsonObject = send(request);
        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");

        while (searchHits != null && searchHits.size() > 0) {
            logger.info("Current " + fieldName + " records: " + results.size() + " collecting...");
            for (int i = 0; i < searchHits.size(); i++) {
                String value = searchHits.get(i).getAsJsonObject().get("_source").getAsJsonObject().get(fieldName).getAsString();
                results.add(value);
            }

            Request scrollRequest = new Request("POST", SCROLL_ENDPOINT);
            String scrollId = jsonObject.get("_scroll_id").getAsString();
            Map<String, Object> scrollQuery = Map.of(
                    "scroll", "10S",
                    "scroll_id", scrollId
            );
            scrollRequest.setJsonEntity(gson.toJson(scrollQuery));
            jsonObject = send(scrollRequest);
            searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        }

        String scrollId = jsonObject.get("_scroll_id").getAsString();
        Request clearScrollRequest = new Request("DELETE", SCROLL_ENDPOINT);
        clearScrollRequest.setJsonEntity("{\"scroll_id\":\"" + scrollId +"\"}");
        send(clearScrollRequest);

        return results;
    }

    public int getTotalHits(JsonObject jsonObject) {
        return jsonObject.get("hits").getAsJsonObject().get("total").getAsJsonObject().get("value").getAsInt();
    }

    public List<Map<String, Object>> collectPage(Request request, Map<String, Object> query, String[][] properties, int pageSize, int offset) throws IOException {
        // data over limit of Elasticsearch, have to use roll API
        if (pageSize + offset > MAX_ES_SIZE) {
            return collectPageWithRoll(request, query, properties, pageSize, offset);
        }

        // data within limit can use just from/size
        query.put("size", pageSize);
        query.put("from", offset);
        request.setJsonEntity(gson.toJson(query));

        JsonObject jsonObject = send(request);
        return collectPage(jsonObject, properties, pageSize);
    }

    // offset MUST be multiple of pageSize, otherwise the page won't be complete
    private List<Map<String, Object>> collectPageWithRoll(Request request, Map<String, Object> query, String[][] properties, int pageSize, int offset) throws IOException {
        final int optimumSize = ( MAX_ES_SIZE / pageSize ) * pageSize;
        if (offset % pageSize != 0) {
            throw new IOException("'offset' must be multiple of 'first'!");
        }
        query.put("size", optimumSize);
        request.setJsonEntity(gson.toJson(query));
        request.addParameter("scroll", "10S");
        JsonObject page = rollToPage(request, offset);
        return collectPage(page, properties, pageSize, offset % optimumSize);
    }

    private JsonObject rollToPage(Request request, int offset) throws IOException {
        int rolledRecords = 0;
        JsonObject jsonObject = send(request);
        String scrollId = jsonObject.get("_scroll_id").getAsString();
        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        rolledRecords += searchHits.size();

        while (rolledRecords <= offset && searchHits.size() > 0) {
            // Keep roll until correct page
            logger.info("Current records: " + rolledRecords + " collecting...");
            Request scrollRequest = new Request("POST", SCROLL_ENDPOINT);
            Map<String, Object> scrollQuery = Map.of(
                    "scroll", "10S",
                    "scroll_id", scrollId
            );
            scrollRequest.setJsonEntity(gson.toJson(scrollQuery));
            jsonObject = send(scrollRequest);
            scrollId = jsonObject.get("_scroll_id").getAsString();
            searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
            rolledRecords += searchHits.size();
        }

        // Now return page
        scrollId = jsonObject.get("_scroll_id").getAsString();
        Request clearScrollRequest = new Request("DELETE", SCROLL_ENDPOINT);
        clearScrollRequest.setJsonEntity("{\"scroll_id\":\"" + scrollId +"\"}");
        send(clearScrollRequest);
        return jsonObject;
    }

    // Collect a page of data, result will be of pageSize or less if not enough data remains
    public List<Map<String, Object>> collectPage(JsonObject jsonObject, String[][] properties, int pageSize) throws IOException {
        return collectPage(jsonObject, properties, pageSize, 0);
    }

    private List<Map<String, Object>> collectPage(JsonObject jsonObject, String[][] properties, int pageSize, int offset) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();

        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        for (int i = 0; i < searchHits.size(); i++) {
            // skip offset number of documents
            if (i + 1 <= offset) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            for (String[] prop: properties) {
                String propName = prop[0];
                String dataField = prop[1];
                JsonElement element = searchHits.get(i).getAsJsonObject().get("_source").getAsJsonObject().get(dataField);
                row.put(propName, getValue(element));
            }
            data.add(row);
            if (data.size() >= pageSize) {
                break;
            }
        }
        return data;
    }

    // Convert JsonElement into Java collections and primitives
    private Object getValue(JsonElement element) {
        Object value = null;
        if (element == null || element.isJsonNull()) {
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