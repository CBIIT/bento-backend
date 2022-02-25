package gov.nih.nci.bento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.TypeMapper;
import gov.nih.nci.bento.service.ESService;
import gov.nih.nci.bento.service.connector.AbstractClient;
import gov.nih.nci.bento.service.connector.DefaultClient;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Request;
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
    TypeMapper typeMapper;

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

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(10);
        searchSourceBuilder.sort(ES_FIELDS.CASE_ID, SortOrder.ASC);

        SearchRequest request = new SearchRequest();
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
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termsQuery(ES_FIELDS.CASE_MEMBER_OF_STUDY, List.of("NCATS-COP01-CCB070020")));
        searchSourceBuilder.size(1);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ES_INDEX.STUDIES);
        request.source(searchSourceBuilder);
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("clinical_study_designation", "clinical_study_designation");

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).get("clinical_study_designation")).isNotNull();
    }

    @Test
    public void elastic_aggregation_no_caseIds_Test() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Aggregate
        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms(Const.ES_PARAMS.TERMS_AGGS)
                .size(Const.ES_PARAMS.AGGS_SIZE)
                .field(Const.ES_FIELDS.CLINICAL_STUDY);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ES_INDEX.CASES);
        request.source(searchSourceBuilder);

        List<Map<String, Object>> result = esService.elasticSend(null, request, typeMapper.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ES_FIELDS.GROUP)).isNotNull();
        assertThat(result.get(0).get(ES_FIELDS.COUNT)).isNotNull();
    }


    @Test
    public void elastic_aggregation_with_caseIds_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        searchSourceBuilder.query(QueryBuilders.termsQuery(ES_FIELDS.CASE_ID, _caseIds));
        // Set Aggregate
        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms(Const.ES_PARAMS.TERMS_AGGS)
                .size(Const.ES_PARAMS.AGGS_SIZE)
                .field(Const.ES_FIELDS.CLINICAL_STUDY);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ES_INDEX.CASES);
        request.source(searchSourceBuilder);

        List<Map<String, Object>> result = esService.elasticSend(null, request, typeMapper.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ES_FIELDS.GROUP)).isNotNull();
        assertThat(result.get(0).get(ES_FIELDS.COUNT)).isNotNull();
    }

    @Test
    public void fileOverViewTest() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        searchSourceBuilder.query(QueryBuilders.termsQuery(ES_FIELDS.CASE_ID, _caseIds));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(Const.ES_INDEX.FILES);
        request.source(searchSourceBuilder);

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(ES_FIELDS.FILE_NAME, ES_FIELDS.FILE_NAME);
        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ES_FIELDS.FILE_NAME)).isNotNull();
    }
}
