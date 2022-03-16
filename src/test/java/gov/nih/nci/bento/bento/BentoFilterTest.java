package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.BENTO_INDEX;
import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.search.query.filter.RangeFilter;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.ESServiceImpl;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith( SpringRunner.class )
@SpringBootTest
public class BentoFilterTest {

    @Autowired
    ESServiceImpl esService;

    @Autowired
    TypeMapperImpl typeMapper;

    @Autowired
    ConfigurationDAO config;

    List<String> _subjectIds = List.of("BENTO-CASE-9971167", "BENTO-CASE-7356713");
    List<String> _fileNames = List.of("10099_OncotypeDXqRTPCR.txt", "10097_OncotypeDXqRTPCR.txt");
    final String _programNameText = "TAILORx";
    final String _programId = "NCT00310180";
    final String _sampleId = "BENTO-BIOS-5707938";
    final String _tissueType = "Tumor";
    final String _sampleAnatomicSite = "Breast";

    @Before
    public void init() {
//        AbstractClient restClient = new DefaultClient(config);
//        client = restClient.getElasticRestClient();
    }

    @Test
    public void subjectOverview_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(10);
        searchSourceBuilder.sort(BENTO_FIELDS.SUBJECT_ID_NUM, SortOrder.DESC);

        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(searchSourceBuilder);
        Set<String> returnTypes = new HashSet<>();
        returnTypes.add("subject_id_gs");
        returnTypes.add("age_at_index_gs");

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size(), greaterThan(0));
        assertThat(result.get(0), hasKey("subject_id_gs"));
        // Check Size
        assertThat(result.size(), equalTo(10));
    }

    @Test
    public void findSubjectIdsInList_Test() throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAllQuery());
        builder.size(10);
        builder.sort(BENTO_FIELDS.SUBJECT_ID_NUM, SortOrder.DESC);
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.should(QueryBuilders.termsQuery("subject_id.keyword", _subjectIds));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(builder);

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add("subject_id");
        returnTypes.add("program_id");

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size(), greaterThan(0));
        assertThat(result.get(0), hasKey("subject_id"));
        // Check Result as expected size
        assertThat(result.size(), equalTo(2));
    }



    @Test
    public void totalHits_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("PROGRAM")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.PROGRAMS)
                                .source(searchSourceBuilder))
                        .typeMapper(typeMapper.getIntTotal()).build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), greaterThan(0));
    }


    @Test
    // TODO
    public void fileIDsFromListTest() throws  IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder();
//        builder.query(QueryBuilders.matchAllQuery());
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.should(QueryBuilders.termsQuery("file_name", _fileNames));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(builder);

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add("file_id");

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getStrList("file_id"));
    }

    @Test
    public void multiSearchTest() throws IOException {

        SearchSourceBuilder builder = new SearchSourceBuilder();

        builder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.PROGRAMS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_STUDIES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.STUDIES)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_SUBJECTS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_SAMPLES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SAMPLES)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_LAB_PROCEDURES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_FILES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build()
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_PROGRAM)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.PROGRAM+ Const.ES_UNITS.KEYWORD, query))
//                        )
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                // TODO
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PROGRAM)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.PROGRAMS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.PROGRAM_CODE + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_STUDY)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                // TODO
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_STUDY)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_DIAGNOSES)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.DIAGNOSES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_DIAGNOSIS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.DIAGNOSES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_RECURRENCE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD,query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_RECURRENCE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TUMOR_SIZE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.TUMOR_SIZES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TUMOR_SIZE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.TUMOR_SIZES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TUMOR_GRADE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.TUMOR_GRADES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TUMOR_GRADE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.TUMOR_GRADES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_ER_STATUS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.ER_STATUS + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_ER_STATUS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.ER_STATUS + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_PR_STATUS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.PR_STATUS + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PR_STATUS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.PR_STATUS + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_CHEMO)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.CHEMO_REGIMEN + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PR_CHEMMO)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.CHEMO_REGIMEN + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_ENDO_THERAPY)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.ENDO_THERAPIES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_ENDO_THERAPY)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.ENDO_THERAPIES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_MENO_THERAPY)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.MENO_STATUS + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_MENO_STATUS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.MENO_STATUS + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TISSUE_TYPE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.TISSUE_TYPE + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TISSUE_TYPE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.TISSUE_TYPE + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TISSUE_COMPOSITION)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.COMPOSITION + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TISSUE_COMPOSITION)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.COMPOSITION + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_FILE_ASSOCI)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.FILES)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.ASSOCIATION + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_FILE_ASSOCIATION)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.FILES)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.ASSOCIATION + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_FILE_TYPE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.FILES)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.FILE_TYPE + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_FILE_TYPE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.FILES)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.FILE_TYPE + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_ARMS_PROGRAM)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(createArmProgramQuery()))
//                        .typeMapper(typeMapper.getArmProgram()).build(),
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_LAB_PROCEDURES)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.LAB_PROCEDURES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_LAB_PROCEDURES)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createTermsAggSourceTest(BENTO_FIELDS.LAB_PROCEDURES + Const.ES_UNITS.KEYWORD, query)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                // TODO ADD FILTER RANGE QUERY
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_BY_AGE)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SUBJECTS)
//                                .source(esService.createRangeQuery()))
//                        .typeMapper(typeMapper.getRange()).build()


        );
        MultiSearchRequest multiRequests = new MultiSearchRequest();
        requests.forEach(r->multiRequests.add(r.getRequest()));

        Map<String, Object> result = esService.elasticMultiSend(requests);

        long test01 = (long) result.get(Bento_GraphQL_KEYS.NO_OF_PROGRAMS);
        assertThat((int) test01, greaterThan(0));

        long test02 = (long) result.get(Bento_GraphQL_KEYS.NO_OF_STUDIES);
        assertThat((int) test02, greaterThan(0));

        long test03 = (long) result.get(Bento_GraphQL_KEYS.NO_OF_SUBJECTS);
        assertThat((int) test03, greaterThan(0));

        long test04 = (long) result.get(Bento_GraphQL_KEYS.NO_OF_SAMPLES);
        assertThat((int) test04, greaterThan(0));

        long test05 = (long) result.get(Bento_GraphQL_KEYS.NO_OF_LAB_PROCEDURES);
        assertThat((int) test05, greaterThan(0));

        long test06 = (long) result.get(Bento_GraphQL_KEYS.NO_OF_FILES);
        assertThat((int) test06, greaterThan(0));
//
//        List<Map<String,Object>> test07 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_PROGRAM);
//        assertThat(test07.size(), greaterThan(0));
//
//        List<Map<String,Object>> test08 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PROGRAM);
//        assertThat(test08.size(), greaterThan(0));
//
//        List<Map<String,Object>> test09 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_STUDY);
//        assertThat(test09.size(), greaterThan(0));
//
//        List<Map<String,Object>> test10 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_STUDY);
//        assertThat(test10.size(), greaterThan(0));
//
//        List<Map<String,Object>> test11 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_DIAGNOSES);
//        assertThat(test11.size(), greaterThan(0));
//
//        List<Map<String,Object>> test12 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_DIAGNOSIS);
//        assertThat(test12.size(), greaterThan(0));
//
//        List<Map<String,Object>> test13 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_DIAGNOSIS);
//        assertThat(test13.size(), greaterThan(0));
//
//        List<Map<String,Object>> test14 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_RECURRENCE);
//        assertThat(test14.size(), greaterThan(0));
//
//        List<Map<String,Object>> test15 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_RECURRENCE);
//        assertThat(test15.size(), greaterThan(0));
//
//        List<Map<String,Object>> test16 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_TUMOR_SIZE);
//        assertThat(test16.size(), greaterThan(0));
//
//        List<Map<String,Object>> test17 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TUMOR_SIZE);
//        assertThat(test17.size(), greaterThan(0));
//
//        List<Map<String,Object>> test18 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_TUMOR_GRADE);
//        assertThat(test18.size(), greaterThan(0));
//
//        List<Map<String,Object>> test19 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TUMOR_GRADE);
//        assertThat(test19.size(), greaterThan(0));
//
//        List<Map<String,Object>> test20 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_ER_STATUS);
//        assertThat(test20.size(), greaterThan(0));
//
//        List<Map<String,Object>> test21 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_ER_STATUS);
//        assertThat(test21.size(), greaterThan(0));
//
//        List<Map<String,Object>> test22 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_PR_STATUS);
//        assertThat(test22.size(), greaterThan(0));
//
//        List<Map<String,Object>> test23 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PR_STATUS);
//        assertThat(test23.size(), greaterThan(0));
//
//        List<Map<String,Object>> test24 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_CHEMO);
//        assertThat(test24.size(), greaterThan(0));
//
//        List<Map<String,Object>> test25 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PR_STATUS);
//        assertThat(test25.size(), greaterThan(0));
//
//        List<Map<String,Object>> test26 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_ENDO_THERAPY);
//        assertThat(test26.size(), greaterThan(0));
//
//        List<Map<String,Object>> test27 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_ENDO_THERAPY);
//        assertThat(test27.size(), greaterThan(0));
//
//        List<Map<String,Object>> test28 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_MENO_THERAPY);
//        assertThat(test28.size(), greaterThan(0));
//
//        List<Map<String,Object>> test29 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_MENO_STATUS);
//        assertThat(test29.size(), greaterThan(0));
//
//        List<Map<String,Object>> test30 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_TISSUE_TYPE);
//        assertThat(test30.size(), greaterThan(0));
//
//        List<Map<String,Object>> test31 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TISSUE_TYPE);
//        assertThat(test31.size(), greaterThan(0));
//
//        List<Map<String,Object>> test32 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_TISSUE_COMPOSITION);
//        assertThat(test32.size(), greaterThan(0));
//
//        List<Map<String,Object>> test33 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TISSUE_COMPOSITION);
//        assertThat(test33.size(), greaterThan(0));
//
//        List<Map<String,Object>> test34 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_FILE_ASSOCI);
//        assertThat(test34.size(), greaterThan(0));
//
//        List<Map<String,Object>> test35 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_FILE_ASSOCIATION);
//        assertThat(test35.size(), greaterThan(0));
//
//        List<Map<String,Object>> test36 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_FILE_TYPE);
//        assertThat(test36.size(), greaterThan(0));
//
//        List<Map<String,Object>> test37 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_FILE_TYPE);
//        assertThat(test37.size(), greaterThan(0));
//
//        List<Map<String,Object>> test38 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.NO_OF_ARMS_PROGRAM);
//        assertThat(test38.size(), greaterThan(0));
//
//        List<Map<String,Object>> test39 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_LAB_PROCEDURES);
//        assertThat(test39.size(), greaterThan(0));
//
//        List<Map<String,Object>> test40 = (List<Map<String,Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_LAB_PROCEDURES);
//        assertThat(test40.size(), greaterThan(0));
//
//        Map<String, Object> test41 = (Map<String, Object>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_BY_AGE);
//        assertThat(test41.size(), greaterThan(0));
    }

    @Test
    public void getRangeTest() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put(BENTO_FIELDS.AGE_AT_INDEX, List.of());
       SearchRequest request =  new SearchRequest()
                .indices(BENTO_INDEX.SUBJECTS)
                .source(new RangeFilter(
                        FilterParam.builder()
                                .args(args)
                                .selectedField(BENTO_FIELDS.AGE_AT_INDEX)
                                .isExcludeFilter(true)
                                .build())
                        .getSourceFilter()
                );
        Set<String> returnTypes = new HashSet<>();
        returnTypes.add("age_at_index");

        Map<String, Object> result = esService.elasticSend(returnTypes, request, typeMapper.getRange());
        assertThat(result.size(), greaterThan(0));
    }


    public SearchSourceBuilder createArmProgramQuery() {
        // TODO Add Filter Query
        return new SearchSourceBuilder()
                .size(0)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.PROGRAM + Const.ES_UNITS.KEYWORD)
                        .subAggregation(
                                AggregationBuilders
                                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                                        .size(Const.ES_PARAMS.AGGS_SIZE)
                                        .field(BENTO_FIELDS.STUDY_ACRONYM + Const.ES_UNITS.KEYWORD)
                        ));
    }

    @Test
    public void testFilter() throws IOException {

        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(
                QueryBuilders.termsQuery(BENTO_FIELDS.ER_STATUS + Const.ES_UNITS.KEYWORD, List.of("Positive")));
//        bool.filter(
//                QueryBuilders.termsQuery(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD, List.of("A: RS 0-10, assigned endocrine therapy alone", "C: RS 11-25, randomized to chemo + endocrine therapy")));
//        bool.filter(
//                QueryBuilders.termsQuery(BENTO_FIELDS.DIAGNOSES + Const.ES_UNITS.KEYWORD, List.of("Medullary Carcinoma")));


//        QueryBuilder query = QueryBuilders.termQuery(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD, "A: RS 0-10, assigned endocrine therapy alone");
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD));

        SearchSourceBuilder builder2 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.DIAGNOSES + Const.ES_UNITS.KEYWORD));

        SearchSourceBuilder builder3 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD));


        SearchSourceBuilder builder3_Compare = new SearchSourceBuilder()
                .size(0)
                .query(QueryBuilders.matchAllQuery())
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD));

        SearchSourceBuilder builder4 = new SearchSourceBuilder()
                .query(bool)
                .size(0)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.PROGRAM + Const.ES_UNITS.KEYWORD)
                        .subAggregation(
                                AggregationBuilders
                                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                                        .size(Const.ES_PARAMS.AGGS_SIZE)
                                        .field(BENTO_FIELDS.STUDY_ACRONYM + Const.ES_UNITS.KEYWORD)
                        ));


        SearchSourceBuilder builder05 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.ER_STATUS + Const.ES_UNITS.KEYWORD));

        SearchSourceBuilder builder06 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.PR_STATUS + Const.ES_UNITS.KEYWORD));


        SearchSourceBuilder builder07 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.PROGRAM + Const.ES_UNITS.KEYWORD));


        builder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_STUDY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_STUDY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_DIAGNOSES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder2))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_DIAGNOSES + "COMPARE")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder3_Compare))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_ARMS_PROGRAM)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder4))
                        .typeMapper(typeMapper.getArmProgram()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_ER_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder05))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_PR_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder06))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PROGRAM)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder07))
                        .typeMapper(typeMapper.getAggregate()).build()
        );


        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>>  test01Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_STUDY);
        assertThat(test01Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test02Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_STUDY);
        assertThat(test02Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test03Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_DIAGNOSES);
        assertThat(test03Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test04Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_STUDY);
        assertThat(test04Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test05Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_ER_STATUS);
        assertThat(test05Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test06Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.SUBJECT_COUNT_PR_STATUS);
        assertThat(test06Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test07Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PROGRAM);
        assertThat(test07Result.size(), is(greaterThan(0)));

    }


    @Test
    public void fileFileType_Test() throws IOException {

        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(
                QueryBuilders.termsQuery(BENTO_FIELDS.FILE_TYPE + Const.ES_UNITS.KEYWORD, List.of("bam", "bai")));
//        QueryBuilder query = QueryBuilders.termQuery(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD, "A: RS 0-10, assigned endocrine therapy alone");

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD));

        builder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_FILES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(builder))
                        .typeMapper(typeMapper.getIntTotal()).build()
        );


        Map<String, Object> result = esService.elasticMultiSend(requests);
        long test01Result = (long) result.get(Bento_GraphQL_KEYS.NO_OF_FILES);
        assertThat((int) test01Result, is(greaterThan(0)));
    }

    @Test
    public void rangeQuery_Test() throws IOException {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(QueryBuilders.rangeQuery(BENTO_FIELDS.AGE_AT_INDEX).gte(0).lte(100));

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD));

        builder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_BY_AGE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder))
                        .typeMapper(typeMapper.getAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>>  test01Result = (List<Map<String, Object>>) result.get(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_BY_AGE);
        assertThat(test01Result.size(), is(greaterThan(0)));
    }



    public QueryBuilder createBentoBoolFromParams(Map<String, Object> args) {
        Map<String, Object> cloneMap = new HashMap<>(args);
        Set<String> sets = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
        cloneMap.keySet().removeAll(sets);
        BoolQueryBuilder bool = new BoolQueryBuilder();
        // TODO TOBE DELETED
        Map<String, String> keyMap= new HashMap<>();
        // Subject Index
        keyMap.put("diagnoses", "diagnosis" + Const.ES_UNITS.KEYWORD);
        keyMap.put("rc_scores", "recurrence_score");
        keyMap.put("tumor_sizes", "tumor_size" + Const.ES_UNITS.KEYWORD);
        keyMap.put("chemo_regimen", "chemotherapy" + Const.ES_UNITS.KEYWORD);
        keyMap.put("tumor_grades", "tumor_grade" + Const.ES_UNITS.KEYWORD);
        keyMap.put("subject_ids", "subject_id" + Const.ES_UNITS.KEYWORD);
        keyMap.put("studies", "study_info" + Const.ES_UNITS.KEYWORD);
        keyMap.put("meno_status", "menopause_status" + Const.ES_UNITS.KEYWORD);
        keyMap.put("programs", "program" + Const.ES_UNITS.KEYWORD);
        keyMap.put("endo_therapies", "endocrine_therapy" + Const.ES_UNITS.KEYWORD);
        // Files Index
        keyMap.put("file_ids", "file_id" + Const.ES_UNITS.KEYWORD);
        keyMap.put("file_names", "file_name" + Const.ES_UNITS.KEYWORD);
        keyMap.put("sample_ids", "sample_id" + Const.ES_UNITS.KEYWORD);

        cloneMap.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);
            if (list.size() > 0) {
                // TODO consider remove empty string
                bool.filter(
                        QueryBuilders.termsQuery(keyMap.getOrDefault(k, k), (List<String>) args.get(k)));
            }
        });
        return bool.filter().size() > 0 ? bool : QueryBuilders.matchAllQuery();
    }



    public SearchSourceBuilder createTermsAggSource(String field) {
        return new SearchSourceBuilder()
                .size(0)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(field));
    }

    static class Bento_GraphQL_KEYS {
        static final String NO_OF_PROGRAMS = "numberOfPrograms";
        static final String NO_OF_STUDIES = "numberOfStudies";
        static final String NO_OF_LAB_PROCEDURES = "numberOfLabProcedures";
        static final String NO_OF_SUBJECTS = "numberOfSubjects";
        static final String NO_OF_SAMPLES = "numberOfSamples";
        static final String NO_OF_FILES = "numberOfFiles";
        static final String NO_OF_ARMS_PROGRAM = "armsByPrograms";
        static final String SUBJECT_COUNT_PROGRAM = "subjectCountByProgram";
        static final String SUBJECT_COUNT_STUDY = "subjectCountByStudy";
        static final String SUBJECT_COUNT_DIAGNOSES = "subjectCountByDiagnoses";
        static final String SUBJECT_COUNT_RECURRENCE = "subjectCountByRecurrenceScore";
        static final String SUBJECT_COUNT_TUMOR_SIZE = "subjectCountByTumorSize";
        static final String SUBJECT_COUNT_TUMOR_GRADE = "subjectCountByTumorGrade";
        static final String SUBJECT_COUNT_ER_STATUS = "subjectCountByErStatus";
        static final String SUBJECT_COUNT_PR_STATUS = "subjectCountByPrStatus";
        static final String SUBJECT_COUNT_CHEMO = "subjectCountByChemotherapyRegimen";
        static final String SUBJECT_COUNT_ENDO_THERAPY = "subjectCountByEndocrineTherapy";
        static final String SUBJECT_COUNT_MENO_THERAPY = "subjectCountByMenopauseStatus";
        static final String SUBJECT_COUNT_TISSUE_TYPE = "subjectCountByTissueType";
        static final String SUBJECT_COUNT_TISSUE_COMPOSITION = "subjectCountByTissueComposition";
        static final String SUBJECT_COUNT_FILE_ASSOCI = "subjectCountByFileAssociation";

        static final String SUBJECT_COUNT_FILE_TYPE = "subjectCountByFileType";
        static final String FILTER_SUBJECT_CNT_PROGRAM = "filterSubjectCountByProgram";
        static final String FILTER_SUBJECT_CNT_STUDY = "filterSubjectCountByStudy";
        static final String FILTER_SUBJECT_CNT_DIAGNOSIS = "filterSubjectCountByDiagnoses";
        static final String FILTER_SUBJECT_CNT_RECURRENCE = "filterSubjectCountByRecurrenceScore";
        static final String FILTER_SUBJECT_CNT_TUMOR_SIZE = "filterSubjectCountByTumorSize";
        static final String FILTER_SUBJECT_CNT_TUMOR_GRADE = "filterSubjectCountByTumorGrade";
        static final String FILTER_SUBJECT_CNT_ER_STATUS = "filterSubjectCountByErStatus";
        static final String FILTER_SUBJECT_CNT_PR_STATUS = "filterSubjectCountByPrStatus";
        static final String FILTER_SUBJECT_CNT_PR_CHEMMO = "filterSubjectCountByChemotherapyRegimen";
        static final String FILTER_SUBJECT_CNT_ENDO_THERAPY = "filterSubjectCountByEndocrineTherapy";
        static final String FILTER_SUBJECT_CNT_MENO_STATUS = "filterSubjectCountByMenopauseStatus";
        static final String FILTER_SUBJECT_CNT_TISSUE_TYPE = "filterSubjectCountByTissueType";
        static final String FILTER_SUBJECT_CNT_TISSUE_COMPOSITION = "filterSubjectCountByTissueComposition";
        static final String FILTER_SUBJECT_CNT_FILE_ASSOCIATION = "filterSubjectCountByFileAssociation";
        static final String FILTER_SUBJECT_CNT_FILE_TYPE = "filterSubjectCountByFileType";
        static final String FILTER_SUBJECT_CNT_BY_AGE = "filterSubjectCountByAge";

        static final String SUBJECT_COUNT_LAB_PROCEDURES = "subjectCountByLabProcedures";
        static final String FILTER_SUBJECT_CNT_LAB_PROCEDURES = "filterSubjectCountByLabProcedures";

        static final String PROGRAM_COUNT = "program_count";
        static final String PROGRAMS = "programs";
        static final String STUDY_COUNT = "study_count";
        static final String STUDIES = "studies";
        static final String SUBJECT_COUNT = "subject_count";
        static final String SUBJECTS = "subjects";
        static final String SAMPLE_COUNT = "sample_count";
        static final String SAMPLES = "samples";
        static final String FILE_COUNT = "file_count";
        static final String FILES = "files";
        static final String ABOUT_COUNT = "about_count";
        static final String ABOUT_PAGE = "about_page";
        static final String MODEL_COUNT = "model_count";
        static final String MODEL = "model";

    }



}

