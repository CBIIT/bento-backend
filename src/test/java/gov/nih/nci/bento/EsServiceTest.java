package gov.nih.nci.bento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.ReturnTypeMapperImpl;
import gov.nih.nci.bento.service.ESService;
import gov.nih.nci.bento.service.connector.AbstractClient;
import gov.nih.nci.bento.service.connector.DefaultClient;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static gov.nih.nci.bento.constants.Const.ES_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith( SpringRunner.class )
@SpringBootTest
public class EsServiceTest {

    @Autowired
    ESService esService;

    @Autowired
    ReturnTypeMapperImpl typeMapper;

    @Autowired
    private ConfigurationDAO config;

    private Gson gson = new GsonBuilder().serializeNulls().create();
    private RestHighLevelClient client;
    private Map<String, Object> query;
    private List<String> _caseIds;

    @Before
    public void init() throws IOException {
        AbstractClient restClient = new DefaultClient(config);
        client = restClient.getElasticRestClient();

        Map<String, Object> params = new HashMap<>();
        params.put("subject_ids", new ArrayList<>());
        query = esService.buildFacetFilterQuery(params);
        // Set Example Case Ids
        _caseIds = Arrays.asList("NCATS-COP01-CCB010015", "GLIOMA01-i_64C0");
    }

    @Test
    public void testbuildListQuery() {
        Map<String, Object> params = Map.of(
                "param1", List.of("value1", "value2")
        );
        Map<String, Object> builtQuery = esService.buildListQuery(params, Set.of());
        assertNotNull(builtQuery);
        var query = (Map<String, Object>)builtQuery.get("query");
        assertNotNull(query);
        var bool = (Map<String, Object>)query.get("bool");
        assertNotNull(bool);
        var filter = (List<Map<String, Object>>)bool.get("filter");
        assertNotNull(filter);
        assertEquals(1, filter.size());
        var param1 = ((Map<String, List<String>>)filter.get(0).get("terms")).get("param1");
        assertEquals(2, param1.size());
        assertEquals("value1", param1.get(0));
        assertEquals("value2", param1.get(1));
    }

    @Test
    public void send_Test() throws IOException {

        final String index = "/samples/_count";
        Request sampleCountRequest = new Request("GET", index);
        sampleCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(sampleCountRequest);
        int count = jsonObject.get("count").getAsInt();
        assertThat(count).isGreaterThan(0);
    }

    @Test
    public void elastic_Test() throws IOException {

        SearchRequest request = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(10);
        searchSourceBuilder.sort(ES_FIELDS.CASE_ID, SortOrder.ASC);

        request.indices(Const.ES_INDEX.CASES);
        request.source(searchSourceBuilder);
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("case_id", "case_id");
        returnTypes.put("cohort", "cohort");


        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.size()).isEqualTo(10);
    }

    // COUNT TEST
    @Test
    public void elastic_query_one_case_Test() throws IOException {
        SearchRequest request = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termsQuery(ES_FIELDS.CASE_MEMBER_OF_STUDY, List.of("NCATS-COP01-CCB070020")));
        searchSourceBuilder.size(1);

        request.indices(Const.ES_INDEX.STUDIES);
        request.source(searchSourceBuilder);
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("clinical_study_designation", "clinical_study_designation");

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).get("clinical_study_designation")).isNotNull();
    }

    // TODO WORKING ON AGGREGATION
    @Test
    public void elastic_aggregation_many_cases_Test() throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices(Const.ES_INDEX.CASES);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
//        searchSourceBuilder.query(QueryBuilders.termsQuery(ES_FIELDS.CASE_MEMBER_OF_STUDY, _caseIds));
        // Set Aggregate
        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms(Const.ES_PARAMS.TERMS_AGGS)
                .field(Const.ES_FIELDS.CLINICAL_STUDY);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.size(0);
        request.source(searchSourceBuilder);

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("clinical_study_designation", "clinical_study_designation");

        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);

        System.out.println("s");
//        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
//        assertThat(result.size()).isGreaterThan(0);
//        assertThat(result.size()).isEqualTo(1);
//        assertThat(result.get(0).get("clinical_study_designation")).isNotNull();


//        Query query = new Query.Builder()
//            .bool(
//                new BoolQuery.Builder()
//                        .should(queries).build()
//            ).build();
//
//        final Aggregation termAgg = Aggregation.of(a -> a.terms(v -> v.field(Const.ES_FIELDS.CLINICAL_STUDY))                                );
//        SearchRequest request = SearchRequest.of(r->r
//                .index(Const.ES_INDEX.CASES)
//                .size(0)
//                .query(query)
//                .aggregations(Const.ES_PARAMS.TERMS_AGGS,termAgg)
//        );
//
//        List<Map<String, Object>> result = esService.elasticSend(null, request, esService.getAggregate());
//        assertThat(result.size()).isGreaterThan(0);
//        assertThat(result.get(0).get(ES_FIELDS.COUNT)).isNotNull();
//        assertThat(result.get(0).get(ES_FIELDS.GROUP)).isNotNull();
    }
//
//
//    @Test
//    public void elastic_aggregation_no_cases_Test() throws IOException {
//
//        final Aggregation termAgg = Aggregation.of(a -> a.terms(v -> v.field(ES_FIELDS.CLINICAL_STUDY))                                );
//        SearchRequest request = SearchRequest.of(r->r
//                .index(Const.ES_INDEX.CASES)
//                .size(0)
//                .aggregations(Const.ES_PARAMS.TERMS_AGGS, termAgg)
//        );
//
//        List<Map<String, Object>> result = esService.elasticSend(null, request, esService.getAggregate());
//        assertThat(result.size()).isGreaterThan(0);
//        assertThat(result.get(0).get(ES_FIELDS.COUNT)).isNotNull();
//        assertThat(result.get(0).get(ES_FIELDS.GROUP)).isNotNull();
//    }
//
//    @Test
//    public void fileOverViewTest() throws IOException {
//
//
//        Query query = new Query.Builder()
//                .matchAll(v->v.queryName("TEST")).build();
//
//        SearchRequest request = SearchRequest.of(r->r
//                .index(Const.ES_INDEX.FILES)
//                .sort(s -> s.field(f -> f.field(ES_FIELDS.FILE_NAME).order(SortOrder.Asc)))
//                .size(10)
//                .from(0)
//                .query(query));
//
//        Map<String, String> returnTypes = new HashMap<>();
//        returnTypes.put("file_name", "file_name");
//        returnTypes.put("file_type", "file_type");
//        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request,esService.getDefault());
//        assertThat(result.size()).isGreaterThan(0);
//    }
//
//
//    private List<Map<String, Object>> fileOverview(QueryParam param, String sortDirection) throws IOException {
//        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
//        Query query = new Query.Builder()
//                .matchAll(v->v.queryName("FILE_OVERVIEW"))
//                .build();
//        Map<String, Object> args = param.getArgs();
//        int pageSize = (int) args.get(Const.ES_PARAMS.PAGE_SIZE);
//        int offset = (int) args.get(Const.ES_PARAMS.OFFSET);
//        String sortField = args.get(Const.ES_PARAMS.ORDER_BY).equals("") ? "file_name" : (String) args.get(Const.ES_PARAMS.ORDER_BY);
//        SearchRequest request = SearchRequest.of(r->r
//                .index(Const.ES_INDEX.FILES)
//                .sort(s ->
//                        s.field(f ->
//                                f.field(sortField).order(getSortType(sortDirection))))
//                .size(pageSize)
//                .from(offset)
//                .query(query));
//
//        return esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());
//    }
//

}
