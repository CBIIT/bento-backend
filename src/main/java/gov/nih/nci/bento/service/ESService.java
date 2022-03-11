package gov.nih.nci.bento.service;

import com.google.gson.*;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.ITypeMapper;
import gov.nih.nci.bento.service.connector.AbstractClient;
import gov.nih.nci.bento.service.connector.DefaultClient;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

@Service("ESService")
@RequiredArgsConstructor
public class ESService {
    public static final String SCROLL_ENDPOINT = "/_search/scroll";
    public static final String JSON_OBJECT = "jsonObject";
    public static final String AGGS = "aggs";
    public static final int MAX_ES_SIZE = 10000;
    private static final Logger logger = LogManager.getLogger(RedisService.class);
    private final ConfigurationDAO config;
    private RestClient client;
    private RestHighLevelClient elasticClient;

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @PostConstruct
    public void init() {
        logger.info("Initializing Elasticsearch client");
        // Base on host name to use signed request (AWS) or not (local)
        // TODO Restconnector TOBE DELETED
        AbstractClient restConnector = new DefaultClient(config);
        AbstractClient elasticConnector = new DefaultClient(config);
        client = restConnector.getRestConnector();
        elasticClient = elasticConnector.getElasticRestClient();
    }

    @PreDestroy
    private void close() throws IOException {
        client.close();
        elasticClient.close();
        elasticClient=null;
    }

    public <T> T elasticSend(Map<String, String> resultType, SearchRequest request, ITypeMapper mapper) throws IOException {

        SearchResponse searchResponse = elasticClient.search(request, RequestOptions.DEFAULT);
        return (T) mapper.getResolver(searchResponse, resultType);
    }

    public Map<String, Object> elasticMultiSend(List<MultipleRequests> requests) throws IOException {
        MultiSearchRequest multiRequests = new MultiSearchRequest();
        requests.forEach(r->multiRequests.add(r.getRequest()));
        Map<String, Object> result = new HashMap<>();
        try {
            MultiSearchResponse response = elasticClient.msearch(multiRequests, RequestOptions.DEFAULT);
            MultiSearchResponse.Item[] responseResponses = response.getResponses();
            final int[] index = {0};

            List.of(responseResponses).forEach(item->{
                MultipleRequests data = requests.get(index[0]);
                result.put(data.getName(),data.getTypeMapper().getResolver(item.getResponse(),null));
                index[0] += 1;
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }

        return result;
    }

    public JsonObject send(Request request) throws IOException {
        Response response = null;
        try {
            response = client.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                String msg = "Elasticsearch returned code: " + statusCode;
                logger.error(msg);
                throw new IOException(msg);
            }
        } catch (Exception e) {
            logger.error(e.toString());
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
        return buildListQuery(params, excludedParams, false);
    }

    public Map<String, Object> buildListQuery(Map<String, Object> params, Set<String> excludedParams, boolean ignoreCase) {
        Map<String, Object> result = new HashMap<>();

        List<Object> filter = new ArrayList<>();
        for (var key: params.keySet()) {
            if (excludedParams.contains(key)) continue;
            Object obj = params.get(key);
            List<String> valueSet = (obj instanceof List) ? (List<String>) obj : List.of((String)obj);

            if (ignoreCase) {
                List<String> lowerCaseValueSet = new ArrayList<>();
                for (String value: valueSet) {
                    lowerCaseValueSet.add(value.toLowerCase());
                }
                valueSet = lowerCaseValueSet;
            }
            // list with only one empty string [""] means return all records
            if (valueSet.size() == 1 && valueSet.get(0).equals("")) continue;
            filter.add(Map.of(
                    "terms", Map.of( key, valueSet)
            ));
        }

        result.put("query", Map.of("bool", Map.of("filter", filter)));
        return result;
    }

    public Map<String, Object> buildFacetFilterQuery(Map<String, Object> params) throws IOException {
        return buildFacetFilterQuery(params, Set.of());
    }

    public Map<String, Object> buildFacetFilterQuery(Map<String, Object> params, Set<String> rangeParams)  throws IOException {
        return buildFacetFilterQuery(params, rangeParams, Set.of());
    }

    public Map<String, Object> buildFacetFilterQuery(Map<String, Object> params, Set<String> rangeParams, Set<String> excludedParams) throws IOException {
        Map<String, Object> result = new HashMap<>();

        List<Object> filter = new ArrayList<>();
        for (var key: params.keySet()) {
            if (excludedParams.contains(key)) continue;

            if (rangeParams.contains(key)) {
                // Range parameters, should contain two doubles, first lower bound, then upper bound
                // Any other values after those two will be ignored
                List<Double> bounds = (List<Double>) params.get(key);
                if (bounds.size() >= 2) {
                    Double lower = bounds.get(0);
                    Double higher = bounds.get(1);
                    if (lower == null && higher == null) {
                        throw new IOException("Lower bound and Upper bound can't be both null!");
                    }
                    Map<String, Double> range = new HashMap<>();
                    if (lower != null) {
                        range.put("gte", lower);
                    }
                    if (higher != null) {
                        range.put("lte", higher);
                    }
                    filter.add(Map.of(
                            "range", Map.of( key, range)
                    ));
                }
            } else {
                // Term parameters (default)
                List<String> valueSet = (List<String>) params.get(key);
                if (valueSet.size() > 0) {
                    filter.add(Map.of(
                            "terms", Map.of( key, valueSet)
                    ));
                }
            }
        }

        result.put("query", Map.of("bool", Map.of("filter", filter)));
        return result;
    }

    public Map<String, Object> addAggregations(Map<String, Object> query, String[] termAggNames) {
        return addAggregations(query, termAggNames, new String[]{});
    }

    public Map<String, Object> addAggregations(Map<String, Object> query, String[] termAggNames, String[] rangeAggNames) {
        Map<String, Object> newQuery = new HashMap<>(query);
        newQuery.put("size", 0);
        newQuery.put("aggregations", getAllAggregations(termAggNames, rangeAggNames));
        return newQuery;
    }

    public void addSubAggregations(Map<String, Object> query, String mainAggName, String[] subTermAggNames) {
        addSubAggregations(query, mainAggName, subTermAggNames, new String[]{});
    }

    public void addSubAggregations(Map<String, Object> query, String mainAggName, String[] subTermAggNames, String[] subRangeAggNames) {
        Map<String, Object> mainAgg = (Map<String, Object>) ((Map<String, Object>) query.get("aggregations")).get(mainAggName);
        Map<String, Object> subAggs = getAllAggregations(subTermAggNames, subRangeAggNames);
        mainAgg.put("aggregations", subAggs);
    }

    private Map<String, Object> getAllAggregations(String[]  termAggNames, String[] rangeAggNames) {
        Map<String, Object> aggs = new HashMap<>();
        for (String aggName: termAggNames) {
            // Terms
            aggs.put(aggName, getTermAggregation(aggName));
        }

        for (String aggName: rangeAggNames) {
            // Range
            aggs.put(aggName, getRangeAggregation(aggName));
        }
        return aggs;
    }

    private Map<String, Object> getTermAggregation(String aggName) {
        Map<String, Object> agg = new HashMap<>();
        agg.put("terms", Map.of("field", aggName, "size", MAX_ES_SIZE));
        return agg;
    }

    private Map<String, Object> getRangeAggregation(String aggName) {
        Map<String, Object> agg = new HashMap<>();
        agg.put("stats", Map.of("field", aggName));
        return agg;
    }

    public Map<String, JsonArray> collectTermAggs(JsonObject jsonObject, String[] termAggNames) {
        Map<String, JsonArray> data = new HashMap<>();
        JsonObject aggs = jsonObject.getAsJsonObject("aggregations");
        for (String aggName: termAggNames) {
            // Terms buckets
            data.put(aggName, aggs.getAsJsonObject(aggName).getAsJsonArray("buckets"));
        }
        return data;
    }

    public List<String> collectTerms(JsonObject jsonObject, String aggName) {
        List<String> data = new ArrayList<>();
        JsonObject aggs = jsonObject.getAsJsonObject("aggregations");
        JsonArray buckets = aggs.getAsJsonObject(aggName).getAsJsonArray("buckets");
        for (var bucket: buckets) {
            data.add(bucket.getAsJsonObject().get("key").getAsString());
        }
        return data;
    }

    public Map<String, JsonObject> collectRangeAggs(JsonObject jsonObject, String[] rangeAggNames) {
        Map<String, JsonObject> data = new HashMap<>();
        JsonObject aggs = jsonObject.getAsJsonObject("aggregations");
        for (String aggName: rangeAggNames) {
            // Range/stats
            data.put(aggName, aggs.getAsJsonObject(aggName));
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
        return collectPage(jsonObject, properties, null, pageSize, offset);
    }

    public List<Map<String, Object>> collectPage(JsonObject jsonObject, String[][] properties, String[][] highlights, int pageSize, int offset) {
        List<Map<String, Object>> result = new ArrayList<>();

        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        for (int i = 0; i < searchHits.size(); i++) {
            // skip offset number of documents
            if (i + 1 <= offset) continue;

            Map<String, Object> row = new HashMap<>();
            for (String[] prop: properties) {
                String propName = prop[0];
                String dataField = prop[1];
                JsonElement element = searchHits.get(i).getAsJsonObject().get("_source").getAsJsonObject().get(dataField);
                row.put(propName, getValue(element));
            }
            // TODO Get Only One Highlight Element
            if (highlights != null) {
                for (String[] highlight: highlights) {
                    String hlName = highlight[0];
                    String hlField = highlight[1];
                    JsonElement element = searchHits.get(i).getAsJsonObject().get("highlight").getAsJsonObject().get(hlField);
                    if (element != null) {
                        row.put(hlName, ((List<String>)getValue(element)).get(0));
                    }
                }
            }
            result.add(row);
            if (result.size() >= pageSize) {
                break;
            }
        }
        return result;
    }

    // Convert JsonElement into Java collections and primitives
    private Object getValue(JsonElement element) {
        Object value = null;
        if (element == null || element.isJsonNull()) return null;
        if (element.isJsonObject()) {
            value = new HashMap<String, Object>();
            JsonObject object = element.getAsJsonObject();
           for (String key: object.keySet()) {
                ((Map<String, Object>) value).put(key, getValue(object.get(key)));
            }
            return value;
        } else if (element.isJsonArray()) {
            value = new ArrayList<>();
            for (JsonElement entry: element.getAsJsonArray()) {
                ((List<Object>)value).add(getValue(entry));
            }
            return value;
        }
        return element.getAsString();
    }

    public SearchSourceBuilder createSourceBuilder(QueryParam param) {
        return new SearchSourceBuilder()
                .query(
                        createBentoBoolFromParams(param.getArgs())
                );
        // TODO consider set size
    }

    public QueryParam CreateQueryParam(DataFetchingEnvironment env) {
        return QueryParam.builder()
                .args(env.getArguments())
                .outputType(env.getFieldType())
                .build();
    }

    public SearchSourceBuilder createTermsAggSourceFilter(String field, Map<String, Object> args) {
        QueryBuilder queryBuilder = createFilterQuery(field, args);
        return new SearchSourceBuilder()
                .size(0)
                .query(queryBuilder)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(field));

    }
    // TODO
    private QueryBuilder createFilterQuery(String field, Map<String, Object> args) {

        Map<String, Object> cloneMap = new HashMap<>(args);
        Set<String> sets = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
        cloneMap.keySet().removeAll(sets);
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // TODO TOBE DELETED
        Map<String, String> keyMap= getQueryParamMap();

        cloneMap.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);

            String key = keyMap.getOrDefault(k, k);
            if (list.size() > 0 && !key.replace(Const.ES_UNITS.KEYWORD, "").equals(field.replace(Const.ES_UNITS.KEYWORD, ""))) {
                // TODO
                if (key.equals(Const.BENTO_FIELDS.AGE_AT_INDEX)) {

                    bool.filter(QueryBuilders.rangeQuery(Const.BENTO_FIELDS.AGE_AT_INDEX)
                            .gte(list.get(0))
                            .lte(list.get(1)));
                } else {
                    // TODO consider remove empty string
                    bool.filter(
                            QueryBuilders.termsQuery(keyMap.getOrDefault(k, k), (List<String>) args.get(k)));
                }
            }
        });
        return bool.filter().size() > 0 ? bool : QueryBuilders.matchAllQuery();
    }

    // TODO temporary use
    private Map<String, String> getQueryParamMap() {
        Map<String, String> keyMap= new HashMap<>();
        // Subject Index
        keyMap.put("diagnoses", "diagnosis" + Const.ES_UNITS.KEYWORD);
        keyMap.put("rc_scores", Const.BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD);
        keyMap.put("tumor_sizes", "tumor_size" + Const.ES_UNITS.KEYWORD);
        keyMap.put("chemo_regimen", "chemotherapy" + Const.ES_UNITS.KEYWORD);
        keyMap.put("tumor_grades", "tumor_grade" + Const.ES_UNITS.KEYWORD);
        keyMap.put("subject_ids", "subject_id" + Const.ES_UNITS.KEYWORD);
        keyMap.put("studies", "study_info" + Const.ES_UNITS.KEYWORD);
        keyMap.put("meno_status", "menopause_status" + Const.ES_UNITS.KEYWORD);
        keyMap.put("programs", "program" + Const.ES_UNITS.KEYWORD);
        keyMap.put("er_status", "er_status" + Const.ES_UNITS.KEYWORD);
        keyMap.put("pr_status", "pr_status" + Const.ES_UNITS.KEYWORD);
        keyMap.put("endo_therapies", "endocrine_therapy" + Const.ES_UNITS.KEYWORD);
        keyMap.put("tissue_type", "tissue_type" + Const.ES_UNITS.KEYWORD);
        keyMap.put("composition", "composition" + Const.ES_UNITS.KEYWORD);
        keyMap.put("association", "association" + Const.ES_UNITS.KEYWORD);
        keyMap.put("file_type", "file_type" + Const.ES_UNITS.KEYWORD);
        keyMap.put("age_at_index", "age_at_index");

        // Files Index
        keyMap.put("file_ids", "file_id" + Const.ES_UNITS.KEYWORD);
        keyMap.put("file_names", "file_name" + Const.ES_UNITS.KEYWORD);
        keyMap.put("sample_ids", "sample_id" + Const.ES_UNITS.KEYWORD);
        return keyMap;
    }

    public QueryBuilder createQuery(Map<String, Object> args) {
        Map<String, Object> cloneMap = new HashMap<>(args);
        Set<String> sets = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
        cloneMap.keySet().removeAll(sets);
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // TODO TOBE DELETED
        Map<String, String> keyMap= getQueryParamMap();

        cloneMap.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);
            if (list.size() > 0) {

                // TODO
                if (k.equals(Const.BENTO_FIELDS.AGE_AT_INDEX)) {

                    bool.filter(QueryBuilders.rangeQuery(Const.BENTO_FIELDS.AGE_AT_INDEX)
                            .gte(list.get(0))
                            .lte(list.get(1)));
                } else {
                    // TODO consider remove empty string
                    bool.filter(
                            QueryBuilders.termsQuery(keyMap.getOrDefault(k, k), (List<String>) args.get(k)));

                }
            }

        });
        return bool.filter().size() > 0 ? bool : QueryBuilders.matchAllQuery();
    }


    public SearchSourceBuilder createTermsAggSourceTestTest(String field, Map<String, Object> args) {
        // TODO
        return new SearchSourceBuilder()
                .size(0)
                .query(createQuery(args))
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(field));
    }

    public SearchSourceBuilder createTermsAggSourceTest(String field, QueryBuilder query) {
        return new SearchSourceBuilder()
                .size(0)
                .query(QueryBuilders.matchAllQuery())
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(field));
    }


    public SearchSourceBuilder createRangeQuery_Test(Map<String, Object> args) {
        QueryBuilder query = createFilterQuery(Const.BENTO_FIELDS.AGE_AT_INDEX, args);
        return new SearchSourceBuilder()
                .size(0)
                .query(query)
                .aggregation(AggregationBuilders
                        .max("max").field(Const.BENTO_FIELDS.AGE_AT_INDEX))
                .aggregation(AggregationBuilders
                        .min("min").field(Const.BENTO_FIELDS.AGE_AT_INDEX));
    }

    // TODO Filter Param
    // TODO To Be DELETED
    public SearchSourceBuilder createRangeQuery() {
        SearchSourceBuilder builder = new SearchSourceBuilder();
//        Map<String, Object> params = Map.of(
//
//                "age_at_index", Set.of(0,100)
//        );
        return new SearchSourceBuilder()
                .size(0)
                .aggregation(AggregationBuilders
                        .max("max").field(Const.BENTO_FIELDS.AGE_AT_INDEX))
                .aggregation(AggregationBuilders
                        .min("min").field(Const.BENTO_FIELDS.AGE_AT_INDEX));
    }

    public SearchSourceBuilder createPageSourceBuilder(QueryParam param, String defaultField) {
        Map<String, Object> args = param.getArgs();
        return new SearchSourceBuilder()
                .query(
                        createBentoBoolFromParams(args)
                )
                .from(param.getOffSet())
                .sort(
                        // TODO Keyword OrderBY
                        param.getOrderBy().equals("") ? defaultField : param.getOrderBy(),
                        param.getSortDirection())
                .size(param.getPageSize());
    }

    // TODO REMOVE DEPENDENCY
    public QueryBuilder createBentoBoolFromParams(Map<String, Object> args) {
        Map<String, Object> cloneMap = new HashMap<>(args);
        Set<String> sets = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
        cloneMap.keySet().removeAll(sets);
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // TODO TOBE DELETED
        Map<String, String> keyMap= getQueryParamMap();
        cloneMap.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);
            if (list.size() > 0) {
                // TODO consider remove empty string
                bool.filter(
                        QueryBuilders.termsQuery(keyMap.getOrDefault(k, k + Const.ES_UNITS.KEYWORD), (List<String>) args.get(k)));
            }
        });
        return bool.filter().size() > 0 ? bool : QueryBuilders.matchAllQuery();
    }
}