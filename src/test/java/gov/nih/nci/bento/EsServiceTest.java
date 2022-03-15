package gov.nih.nci.bento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.ESServiceImpl;
import gov.nih.nci.bento.service.connector.AbstractClient;
import gov.nih.nci.bento.service.connector.DefaultClient;
import org.elasticsearch.action.search.SearchRequest;
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

import static gov.nih.nci.bento.constants.Const.ICDC_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith( SpringRunner.class )
@SpringBootTest
public class EsServiceTest {

    @Autowired
    ESServiceImpl esService;

    @Autowired
    TypeMapperImpl typeMapper;

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
        // TODO
//        query = esService.buildFacetFilterQuery(params);
        // Set Example Case Ids
        _caseIds = Arrays.asList("NCATS-COP01-CCB010015", "GLIOMA01-i_64C0");
    }

    @Test
    public void elastic_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(10);
        searchSourceBuilder.sort(ICDC_FIELDS.CASE_ID, SortOrder.ASC);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ICDC_INDEX.CASES);
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
        searchSourceBuilder.query(QueryBuilders.termsQuery(ICDC_FIELDS.CASE_MEMBER_OF_STUDY, List.of("NCATS-COP01-CCB070020")));
        searchSourceBuilder.size(1);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ICDC_INDEX.STUDIES);
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
                .field(ICDC_FIELDS.CLINICAL_STUDY);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ICDC_INDEX.CASES);
        request.source(searchSourceBuilder);

        List<Map<String, Object>> result = esService.elasticSend(null, request, typeMapper.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ICDC_FIELDS.GROUP)).isNotNull();
        assertThat(result.get(0).get(ICDC_FIELDS.COUNT)).isNotNull();
    }


    @Test
    public void elastic_aggregation_with_caseIds_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        searchSourceBuilder.query(QueryBuilders.termsQuery(ICDC_FIELDS.CASE_ID, _caseIds));
        // Set Aggregate
        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms(Const.ES_PARAMS.TERMS_AGGS)
                .size(Const.ES_PARAMS.AGGS_SIZE)
                .field(ICDC_FIELDS.CLINICAL_STUDY);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ICDC_INDEX.CASES);
        request.source(searchSourceBuilder);

        List<Map<String, Object>> result = esService.elasticSend(null, request, typeMapper.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ICDC_FIELDS.GROUP)).isNotNull();
        assertThat(result.get(0).get(ICDC_FIELDS.COUNT)).isNotNull();
    }

    @Test
    public void fileOverViewTest() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        searchSourceBuilder.query(QueryBuilders.termsQuery(ICDC_FIELDS.CASE_ID, _caseIds));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(Const.ICDC_INDEX.FILES);
        request.source(searchSourceBuilder);

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(ICDC_FIELDS.FILE_NAME, ICDC_FIELDS.FILE_NAME);
        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ICDC_FIELDS.FILE_NAME)).isNotNull();
    }
}
