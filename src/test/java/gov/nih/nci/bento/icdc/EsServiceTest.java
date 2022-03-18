package gov.nih.nci.bento.icdc;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.ESServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static gov.nih.nci.bento.constants.Const.ICDC_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@RunWith( SpringRunner.class )
@SpringBootTest
public class EsServiceTest {

    @Autowired
    ESServiceImpl esService;

    @Autowired
    TypeMapperImpl typeMapper;

    @Test
    public void subject_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(10);

        SearchRequest request = new SearchRequest();
        request.indices(Const.ICDC_INDEX.CASES);
        request.source(searchSourceBuilder);

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.SUBJECT_ID);

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getDefault(returnTypes));
        MatcherAssert.assertThat(result.size(), greaterThan(0));
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
        Set<String> returnTypes = new HashSet<>();
        returnTypes.add("case_id");
        returnTypes.add("cohort");


        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getDefault(returnTypes));
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
        Set<String> returnTypes = new HashSet<>();
        returnTypes.add("clinical_study_designation");

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getDefault(returnTypes));
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

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ICDC_FIELDS.GROUP)).isNotNull();
        assertThat(result.get(0).get(ICDC_FIELDS.COUNT)).isNotNull();
    }


    @Test
    public void elastic_aggregation_with_caseIds_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        searchSourceBuilder.query(QueryBuilders.termsQuery(ICDC_FIELDS.CASE_ID, Arrays.asList("NCATS-COP01-CCB010015", "GLIOMA01-i_64C0")));
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

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ICDC_FIELDS.GROUP)).isNotNull();
        assertThat(result.get(0).get(ICDC_FIELDS.COUNT)).isNotNull();
    }

    @Test
    public void fileOverViewTest() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        searchSourceBuilder.query(QueryBuilders.termsQuery(ICDC_FIELDS.CASE_ID, Arrays.asList("NCATS-COP01-CCB010015", "GLIOMA01-i_64C0")));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(Const.ICDC_INDEX.FILES);
        request.source(searchSourceBuilder);

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(ICDC_FIELDS.FILE_NAME);
        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getDefault(returnTypes));
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ICDC_FIELDS.FILE_NAME)).isNotNull();
    }
}
