package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.BENTO_INDEX;
import gov.nih.nci.bento.search.query.filter.AggregationFilter;
import gov.nih.nci.bento.search.query.filter.RangeFilter;
import gov.nih.nci.bento.search.query.filter.SubAggregationFilter;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.ESServiceImpl;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
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

    @Test
    public void searchAggregationStudiesCnt_Test() throws IOException {

        Map<String, Object> args = new HashMap<>();
        args.put(BENTO_FIELDS.AGE_AT_INDEX, List.of());
        args.put(BENTO_FIELDS.SUBJECT_ID, List.of());

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.STUDIES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregateTotalCnt()).build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        Integer aggResultSize = (Integer) result.get("TEST01");
        assertThat(aggResultSize, greaterThan(0));
    }

    @Test
    public void searchAggregationLabCnt_Test() throws IOException {

        Map<String, Object> args = new HashMap<>();
        args.put(BENTO_FIELDS.AGE_AT_INDEX, List.of());
        args.put(BENTO_FIELDS.SUBJECT_ID, List.of());

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.LAB_PROCEDURES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregateTotalCnt()).build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        Integer aggResultSize = (Integer) result.get("TEST01");
        assertThat(aggResultSize, greaterThan(0));
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

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getList(returnTypes));
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
        bool.should(
                QueryBuilders.termsQuery(BENTO_FIELDS.SUBJECT_ID, List.of("BENTO-CASE-9971167", "BENTO-CASE-7356713"))
        );
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(builder);

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(BENTO_FIELDS.SUBJECT_ID);
        returnTypes.add(BENTO_FIELDS.PROGRAM_ID);

        List<Map<String, Object>> result = esService.elasticSend(request, typeMapper.getList(returnTypes));
        assertThat(result.size(), greaterThan(0));
        assertThat(result.get(0), hasKey(BENTO_FIELDS.SUBJECT_ID));
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
    public void fileIDsFromList_Test() throws  IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.should(QueryBuilders.termsQuery(BENTO_FIELDS.FILE_NAME, List.of("10099_OncotypeDXqRTPCR.txt", "10097_OncotypeDXqRTPCR.txt")));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(builder);

        List<String> result = esService.elasticSend(request, typeMapper.getStrList(BENTO_FIELDS.FILE_ID));
        assertThat(result.size(), greaterThan(0));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void multiSearchTest() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put(BENTO_FIELDS.AGE_AT_INDEX, List.of());
        args.put(BENTO_FIELDS.SUBJECT_ID, List.of());

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_PROGRAMS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.PROGRAMS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_STUDIES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.STUDIES)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_SUBJECTS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_SAMPLES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SAMPLES)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_LAB_PROCEDURES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_FILES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_PROGRAM)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.PROGRAM)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PROGRAM)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.PROGRAM)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_STUDY)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.STUDIES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_STUDY)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.STUDIES)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_DIAGNOSES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.DIAGNOSES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_DIAGNOSIS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.DIAGNOSES)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_RECURRENCE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.RC_SCORES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_RECURRENCE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.RC_SCORES)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_TUMOR_SIZE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.TUMOR_SIZES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TUMOR_SIZE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.TUMOR_SIZES)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_TUMOR_GRADE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.TUMOR_GRADES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TUMOR_GRADE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.TUMOR_GRADES)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_ER_STATUS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.ER_STATUS)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_ER_STATUS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.ER_STATUS)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_PR_STATUS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.PR_STATUS)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PR_STATUS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.PR_STATUS)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_CHEMO)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.CHEMO_REGIMEN)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PR_CHEMMO)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.CHEMO_REGIMEN)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_ENDO_THERAPY)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.ENDO_THERAPIES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_ENDO_THERAPY)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.ENDO_THERAPIES)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_MENO_THERAPY)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.MENO_STATUS)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_MENO_STATUS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.MENO_STATUS)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_TISSUE_TYPE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.TISSUE_TYPE)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TISSUE_TYPE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.TISSUE_TYPE)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_TISSUE_COMPOSITION)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.COMPOSITION)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TISSUE_COMPOSITION)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.COMPOSITION)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_FILE_ASSOCI)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.ASSOCIATION)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_FILE_ASSOCIATION)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.ASSOCIATION)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),



                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_FILE_TYPE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.FILE_TYPE)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_FILE_TYPE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.FILE_TYPE)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_ARMS_PROGRAM)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new SubAggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.PROGRAM)
                                                .subAggSelectedField(Const.BENTO_FIELDS.STUDY_ACRONYM)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getArmProgram()).build(),

                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_LAB_PROCEDURES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.LAB_PROCEDURES)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                // Arm Program
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_LAB_PROCEDURES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.LAB_PROCEDURES)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                // Range Query
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_BY_AGE)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new RangeFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(Const.BENTO_FIELDS.AGE_AT_INDEX)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getRange()).build()


        );
        MultiSearchRequest multiRequests = new MultiSearchRequest();
        requests.forEach(r->multiRequests.add(r.getRequest()));

        Map<String, Object> result = esService.elasticMultiSend(requests);

        long test01 = (long) result.get(BentoGraphQLKEYS.NO_OF_PROGRAMS);
        assertThat((int) test01, greaterThan(0));

        long test02 = (long) result.get(BentoGraphQLKEYS.NO_OF_STUDIES);
        assertThat((int) test02, greaterThan(0));

        long test03 = (long) result.get(BentoGraphQLKEYS.NO_OF_SUBJECTS);
        assertThat((int) test03, greaterThan(0));

        long test04 = (long) result.get(BentoGraphQLKEYS.NO_OF_SAMPLES);
        assertThat((int) test04, greaterThan(0));

        long test05 = (long) result.get(BentoGraphQLKEYS.NO_OF_LAB_PROCEDURES);
        assertThat((int) test05, greaterThan(0));

        long test06 = (long) result.get(BentoGraphQLKEYS.NO_OF_FILES);
        assertThat((int) test06, greaterThan(0));

        List<Map<String,Object>> test07 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_PROGRAM);
        assertThat(test07.size(), greaterThan(0));

        List<Map<String,Object>> test08 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PROGRAM);
        assertThat(test08.size(), greaterThan(0));

        List<Map<String,Object>> test09 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_STUDY);
        assertThat(test09.size(), greaterThan(0));

        List<Map<String,Object>> test10 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_STUDY);
        assertThat(test10.size(), greaterThan(0));

        List<Map<String,Object>> test11 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_DIAGNOSES);
        assertThat(test11.size(), greaterThan(0));

        List<Map<String,Object>> test12 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_DIAGNOSIS);
        assertThat(test12.size(), greaterThan(0));

        List<Map<String,Object>> test13 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_DIAGNOSIS);
        assertThat(test13.size(), greaterThan(0));

        List<Map<String,Object>> test14 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_RECURRENCE);
        assertThat(test14.size(), greaterThan(0));

        List<Map<String,Object>> test15 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_RECURRENCE);
        assertThat(test15.size(), greaterThan(0));

        List<Map<String,Object>> test16 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_TUMOR_SIZE);
        assertThat(test16.size(), greaterThan(0));

        List<Map<String,Object>> test17 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TUMOR_SIZE);
        assertThat(test17.size(), greaterThan(0));

        List<Map<String,Object>> test18 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_TUMOR_GRADE);
        assertThat(test18.size(), greaterThan(0));

        List<Map<String,Object>> test19 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TUMOR_GRADE);
        assertThat(test19.size(), greaterThan(0));

        List<Map<String,Object>> test20 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_ER_STATUS);
        assertThat(test20.size(), greaterThan(0));

        List<Map<String,Object>> test21 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_ER_STATUS);
        assertThat(test21.size(), greaterThan(0));

        List<Map<String,Object>> test22 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_PR_STATUS);
        assertThat(test22.size(), greaterThan(0));

        List<Map<String,Object>> test23 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PR_STATUS);
        assertThat(test23.size(), greaterThan(0));

        List<Map<String,Object>> test24 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_CHEMO);
        assertThat(test24.size(), greaterThan(0));

        List<Map<String,Object>> test25 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PR_STATUS);
        assertThat(test25.size(), greaterThan(0));

        List<Map<String,Object>> test26 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_ENDO_THERAPY);
        assertThat(test26.size(), greaterThan(0));

        List<Map<String,Object>> test27 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_ENDO_THERAPY);
        assertThat(test27.size(), greaterThan(0));

        List<Map<String,Object>> test28 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_MENO_THERAPY);
        assertThat(test28.size(), greaterThan(0));

        List<Map<String,Object>> test29 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_MENO_STATUS);
        assertThat(test29.size(), greaterThan(0));

        List<Map<String,Object>> test30 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_TISSUE_TYPE);
        assertThat(test30.size(), greaterThan(0));

        List<Map<String,Object>> test31 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TISSUE_TYPE);
        assertThat(test31.size(), greaterThan(0));

        List<Map<String,Object>> test32 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_TISSUE_COMPOSITION);
        assertThat(test32.size(), greaterThan(0));

        List<Map<String,Object>> test33 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TISSUE_COMPOSITION);
        assertThat(test33.size(), greaterThan(0));

        List<Map<String,Object>> test34 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_FILE_ASSOCI);
        assertThat(test34.size(), greaterThan(0));

        List<Map<String,Object>> test35 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_FILE_ASSOCIATION);
        assertThat(test35.size(), greaterThan(0));

        List<Map<String,Object>> test36 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_FILE_TYPE);
        assertThat(test36.size(), greaterThan(0));

        List<Map<String,Object>> test37 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_FILE_TYPE);
        assertThat(test37.size(), greaterThan(0));

        List<Map<String,Object>> test38 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.NO_OF_ARMS_PROGRAM);
        assertThat(test38.size(), greaterThan(0));

        List<Map<String,Object>> test39 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_LAB_PROCEDURES);
        assertThat(test39.size(), greaterThan(0));

        List<Map<String,Object>> test40 = (List<Map<String,Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_LAB_PROCEDURES);
        assertThat(test40.size(), greaterThan(0));

        Map<String, Object> test41 = (Map<String, Object>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_BY_AGE);
        assertThat(test41.size(), greaterThan(0));
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

        Map<String, Object> result = esService.elasticSend(request, typeMapper.getRange());
        assertThat(result.size(), greaterThan(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() throws IOException {

        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(
                QueryBuilders.termsQuery(BENTO_FIELDS.ER_STATUS, List.of("Positive")));
//        bool.filter(
//                QueryBuilders.termsQuery(BENTO_FIELDS.STUDIES, List.of("A: RS 0-10, assigned endocrine therapy alone", "C: RS 11-25, randomized to chemo + endocrine therapy")));
//        bool.filter(
//                QueryBuilders.termsQuery(BENTO_FIELDS.DIAGNOSES, List.of("Medullary Carcinoma")));


//        QueryBuilder query = QueryBuilders.termQuery(BENTO_FIELDS.STUDIES, "A: RS 0-10, assigned endocrine therapy alone");
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.STUDIES));

        SearchSourceBuilder builder2 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.DIAGNOSES));

        SearchSourceBuilder builder4 = new SearchSourceBuilder()
                .query(bool)
                .size(0)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.PROGRAM)
                        .subAggregation(
                                AggregationBuilders
                                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                                        .size(Const.ES_PARAMS.AGGS_SIZE)
                                        .field(BENTO_FIELDS.STUDY_ACRONYM)
                        ));


        SearchSourceBuilder builder05 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.ER_STATUS));

        SearchSourceBuilder builder06 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.PR_STATUS));


        SearchSourceBuilder builder07 = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.PROGRAM));


        builder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_STUDY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_STUDY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_DIAGNOSES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder2))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_ARMS_PROGRAM)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder4))
                        .typeMapper(typeMapper.getArmProgram()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_ER_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder05))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.SUBJECT_COUNT_PR_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder06))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PROGRAM)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder07))
                        .typeMapper(typeMapper.getAggregate()).build()
        );


        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>>  test01Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_STUDY);
        assertThat(test01Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test02Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_STUDY);
        assertThat(test02Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test03Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_DIAGNOSES);
        assertThat(test03Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test04Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_STUDY);
        assertThat(test04Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test05Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_ER_STATUS);
        assertThat(test05Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test06Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.SUBJECT_COUNT_PR_STATUS);
        assertThat(test06Result.size(), is(greaterThan(0)));

        List<Map<String, Object>>  test07Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PROGRAM);
        assertThat(test07Result.size(), is(greaterThan(0)));

    }


    @Test
    public void fileFileType_Test() throws IOException {

        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(
                QueryBuilders.termsQuery(BENTO_FIELDS.FILE_TYPE, List.of("bam", "bai")));
//        QueryBuilder query = QueryBuilders.termQuery(BENTO_FIELDS.STUDIES, "A: RS 0-10, assigned endocrine therapy alone");

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.STUDIES));

        builder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.NO_OF_FILES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(builder))
                        .typeMapper(typeMapper.getIntTotal()).build()
        );


        Map<String, Object> result = esService.elasticMultiSend(requests);
        long test01Result = (long) result.get(BentoGraphQLKEYS.NO_OF_FILES);
        assertThat((int) test01Result, is(greaterThan(0)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rangeQuery_Test() throws IOException {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        bool.filter(QueryBuilders.rangeQuery(BENTO_FIELDS.AGE_AT_INDEX).gte(0).lte(100));

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(BENTO_FIELDS.STUDIES));

        builder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_BY_AGE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(builder))
                        .typeMapper(typeMapper.getAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>>  test01Result = (List<Map<String, Object>>) result.get(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_BY_AGE);
        assertThat(test01Result.size(), is(greaterThan(0)));
    }

    static class BentoGraphQLKEYS {
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

