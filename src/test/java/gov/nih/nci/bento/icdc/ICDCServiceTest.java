package gov.nih.nci.bento.icdc;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryResult;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.filter.AggregationFilter;
import gov.nih.nci.bento.search.query.filter.NestedFilter;
import gov.nih.nci.bento.search.query.filter.NestedSumFilter;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.search.yaml.filter.YamlFilterType;
import gov.nih.nci.bento.service.ESServiceImpl;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
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
import static org.hamcrest.Matchers.*;

@RunWith( SpringRunner.class )
@SpringBootTest
public class ICDCServiceTest {

    @Autowired
    ESServiceImpl esService;

    @Autowired
    TypeMapperImpl typeMapper;

    @Test
    public void nestedExcludeFilterSearch_Test() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("file_format", List.of("bam"));
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.CASES)
                                .source(new NestedFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField("file_type")
                                                .nestedPath("files_info")
                                                .isExcludeFilter(true)
                                                // List filter fields
                                                .nestedFields(Set.of("file_format"))
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getICDCNestedAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        QueryResult queryResult = (QueryResult) result.get("TEST01");
        MatcherAssert.assertThat(queryResult.getTotalHits(), is(greaterThan(0)));
    }


    @Test
    public void sumAggregation_Test() throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .aggregation(AggregationBuilders
                        .sum(Const.ES_PARAMS.TERMS_AGGS)
                        .field("file_size"));
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.FILES)
                                .source(builder))
                        .typeMapper(typeMapper.getSumAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>>  test01Result = (List<Map<String, Object>>) result.get("TEST01");
        MatcherAssert.assertThat(test01Result.size(), is(greaterThan(0)));
    }

    @Test
    public void sumNestedAggregation_Test() throws IOException {
        Map<String, Object> args = new HashMap<>();
        List<MultipleRequests> requests = List.of(
        MultipleRequests.builder()
                .name("TEST01")
                .request(new SearchRequest()
                        .indices(Const.ICDC_INDEX.CASES)
                        .source(new NestedSumFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField("file_size")
                                        .nestedPath("files_info")
                                        .nestedFields(Set.of("file_type", "file_format", "file_association"))
                                        .build())
                                .getSourceFilter()))
                .typeMapper(typeMapper.getNestedSumAggregate()).build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        double  test01Result = (double) result.get("TEST01");
        MatcherAssert.assertThat((int) test01Result, is(greaterThan(0)));
    }

    @Test
    public void breedAgg_Test() throws IOException {
        // Argument Match with Fields in Elasticsearch
        Map<String, Object> args = new HashMap<>();
        args.put(ICDC_FIELDS.CASE_ID, List.of(
                "COTC022-0210",
                "COTC022-1004",
                "COTC022-1803",
                "COTC022-2005"
        ));

        // Set Argument Map
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.CASES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(ICDC_FIELDS.BREED)
                                                .build())
                                        .getSourceFilter()))
                        .typeMapper(typeMapper.getICDCAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>> test01Result = (List<Map<String, Object>>) result.get("TEST01");
        MatcherAssert.assertThat(test01Result.size(), is(greaterThan(0)));
    }


    @Test
    public void programCount_Test() throws IOException {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // program_acronym
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.PROGRAM_ACRONYM, List.of("COP")));
        // clinical_study_designation
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.CLINICAL_STUDY, List.of("NCATS-COP01")));
        // breed
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.BREED, List.of("Mixed Breed")));
        // breed
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.SEX, List.of("Male")));
        // neutered_status
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.NEUTERED_STATUS, List.of("Yes")));
        // disease_term
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.DIAGNOSIS, List.of("B Cell Lymphoma")));
        // disease_site
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.DISEASE_SITE, List.of("Lymph Node")));
        // stage_of_disease
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.STAGE_OF_DISEASE, List.of("III")));
        // best_response
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.RESPONSE_TO_TREATMENT, List.of("Not Determined")));
        // best_response
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.CANINE_INDIVIDUAL, List.of("Single Study")));

        // Nested File
        bool.filter(QueryBuilders.nestedQuery("files_info",
                QueryBuilders.termsQuery("files_info" + "." + "association", List.of("sample")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("files_info",
                QueryBuilders.termsQuery("files_info" + "." + "file_type", List.of("RNA Sequence File")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("files_info",
                QueryBuilders.termsQuery("files_info" + "." + "file_format", List.of("bam")),
                ScoreMode.None));
        // Nested Sample
        bool.filter(QueryBuilders.nestedQuery("samples_info",
                QueryBuilders.termsQuery("samples_info" + "." + "sample_type", List.of("Whole Blood")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("samples_info",
                QueryBuilders.termsQuery("samples_info" + "." + "sample_pathology", List.of("Not Applicable")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("samples_info",
                QueryBuilders.termsQuery("samples_info" + "." + "sample_site", List.of("Blood")),
                ScoreMode.None));
        // Nested Registration
        bool.filter(QueryBuilders.nestedQuery("registration_info",
                QueryBuilders.termsQuery("registration_info" + "." + "registration_origin", List.of("NCATS-COP01")),
                ScoreMode.None));
        // OR
//        bool.filter(QueryBuilders.nestedQuery("registration_info",
//                QueryBuilders.termsQuery("registration_info" + "." + "registration_origin", List.of("Not Applicable")),
//                ScoreMode.None));
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(Const.ICDC_FIELDS.PROGRAM_ACRONYM));
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.CASES)
                                .source(builder))
                        .typeMapper(typeMapper.getAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>>  test01Result = (List<Map<String, Object>>) result.get("TEST01");
        MatcherAssert.assertThat(test01Result.size(), is(greaterThan(0)));
    }


    @Test
    public void nestedCount_Test() throws IOException {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // program_acronym
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.PROGRAM_ACRONYM, List.of("COP")));
        // clinical_study_designation
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.CLINICAL_STUDY, List.of("NCATS-COP01")));
        // breed
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.BREED, List.of("Mixed Breed")));
        // breed
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.SEX, List.of("Male")));
        // neutered_status
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.NEUTERED_STATUS, List.of("Yes")));
        // disease_term
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.DIAGNOSIS, List.of("B Cell Lymphoma")));
        // disease_site
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.DISEASE_SITE, List.of("Lymph Node")));
        // stage_of_disease
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.STAGE_OF_DISEASE, List.of("III")));
        // best_response
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.RESPONSE_TO_TREATMENT, List.of("Not Determined")));
        // best_response
        bool.filter(
                QueryBuilders.termsQuery(ICDC_FIELDS.CANINE_INDIVIDUAL, List.of("Single Study")));

        // Nested File
        bool.filter(QueryBuilders.nestedQuery("files_info",
                QueryBuilders.termsQuery("files_info" + "." + "association", List.of("sample")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("files_info",
                QueryBuilders.termsQuery("files_info" + "." + "file_type", List.of("RNA Sequence File")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("files_info",
                QueryBuilders.termsQuery("files_info" + "." + "file_format", List.of("bam")),
                ScoreMode.None));
        // Nested Sample
        bool.filter(QueryBuilders.nestedQuery("samples_info",
                QueryBuilders.termsQuery("samples_info" + "." + "sample_type", List.of("Whole Blood")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("samples_info",
                QueryBuilders.termsQuery("samples_info" + "." + "sample_pathology", List.of("Not Applicable")),
                ScoreMode.None));
        bool.filter(QueryBuilders.nestedQuery("samples_info",
                QueryBuilders.termsQuery("samples_info" + "." + "sample_site", List.of("Blood")),
                ScoreMode.None));
        // Nested Registration
        bool.filter(QueryBuilders.nestedQuery("registration_info",
                QueryBuilders.termsQuery("registration_info" + "." + "registration_origin", List.of("NCATS-COP01")),
                ScoreMode.None));
        // OR
//        bool.filter(QueryBuilders.nestedQuery("registration_info",
//                QueryBuilders.termsQuery("registration_info" + "." + "registration_origin", List.of("Not Applicable")),
//                ScoreMode.None));
//        SearchSourceBuilder builder = new SearchSourceBuilder()
//                .size(0)
//                .query(bool)
//                .aggregation(AggregationBuilders
//                        .terms(Const.ES_PARAMS.TERMS_AGGS)
//                        .size(Const.ES_PARAMS.AGGS_SIZE)
//                        .field(Const.ICDC_FIELDS.PROGRAM_ACRONYM));



        // Argument Match with Fields in Elasticsearch
        Map<String, Object> args = new HashMap<>();
        args.put(ICDC_FIELDS.PROGRAM_ACRONYM, List.of("COP"));


        // Set Argument Map

        YamlFilterType yamlFilterType = new YamlFilterType();
        yamlFilterType.setSelectedField("sample_type");
        yamlFilterType.setNestedPath("samples_info");
        yamlFilterType.setNestedFields(Set.of("sample_type", "sample_site", "sample_pathology"));
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.CASES)
                                .source(new NestedFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(yamlFilterType.getSelectedField())
                                                .nestedPath(yamlFilterType.getNestedPath())
                                                .nestedFields(yamlFilterType.getNestedFields())
                                                .build())
                                        .getSourceFilter()))
                        .typeMapper(typeMapper.getNestedAggregateList()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>> test01Result = (List<Map<String, Object>>) result.get("TEST01");
        MatcherAssert.assertThat(test01Result.size(), is(greaterThan(0)));
    }


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

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getList(returnTypes));
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


        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getList(returnTypes));
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

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getList(returnTypes));
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
        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getList(returnTypes));
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ICDC_FIELDS.FILE_NAME)).isNotNull();
    }
}
