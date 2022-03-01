package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.BENTO_INDEX;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.TypeMapper;
import gov.nih.nci.bento.service.ESService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith( SpringRunner.class )
@SpringBootTest
public class BentoFilterTest {

    @Autowired
    ESService esService;

    @Autowired
    TypeMapper typeMapper;

    @Autowired
    ConfigurationDAO config;

//    private RestHighLevelClient client;

    @Before
    public void init() throws IOException {
//        AbstractClient restClient = new DefaultClient(config);
//        client = restClient.getElasticRestClient();
    }

    @Test
    public void subjectSearchTest() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(searchSourceBuilder);
        int result = esService.elasticSend(null, request, typeMapper.getIntTotal());
        assertThat(result, greaterThan(0));
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
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("subject_id_gs", "subject_id");
        returnTypes.put("age_at_index_gs", "program");

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size(), greaterThan(0));
        assertThat(result.get(0), hasKey("subject_id_gs"));
        // Check Size
        assertThat(result.size(), equalTo(10));
    }


    @Test
    public void totalHits_Test() throws IOException {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("PROGRAM")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_FIELDS.PROGRAMS)
                                .source(searchSourceBuilder))
                        .typeMapper(typeMapper.getIntTotal()).build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), greaterThan(0));
    }


//    @Test
//    public void multiSearchTest() throws IOException {
//
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.size(0);
//        List<MultipleRequests> requests = List.of(
//                MultipleRequests.builder()
//                        .name("numberOfPrograms")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_FIELDS.PROGRAMS)
//                                .source(searchSourceBuilder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name("numberOfStudies")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.STUDIES)
//                                .source(searchSourceBuilder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name("numberOfSubjects")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(searchSourceBuilder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name("numberOfSamples")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SAMPLES)
//                                .source(searchSourceBuilder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name("numberOfLabProcedures")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsQuery()))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name("numberOfFiles")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsQuery()))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name("subjectCountByProgram")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.PROGRAMS))
//                        )
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                // TODO
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByProgram")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.PROGRAMS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.PROGRAMS)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("subjectCountByStudy")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.STUDIES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                // TODO
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByStudy")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.STUDIES)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.STUDIES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("subjectCountByDiagnoses")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.DIAGNOSES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByDiagnoses")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.DIAGNOSES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("subjectCountByRecurrenceScore")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.RC_SCORES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByRecurrenceScore")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.RC_SCORES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByTumorSize")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.TUMOR_SIZES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByTumorSize")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.RC_SCORES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByTumorGrade")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.TUMOR_GRADES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByTumorGrade")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.TUMOR_GRADES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByErStatus")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.ER_STATUS)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByErStatus")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.ER_STATUS)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByPrStatus")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.PR_STATUS)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByPrStatus")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.PR_STATUS)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name("subjectCountByChemotherapyRegimen")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.CHEMO_REGIMEN)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByChemotherapyRegimen")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.CHEMO_REGIMEN)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name("subjectCountByEndocrineTherapy")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.ENDO_THERAPIES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByEndocrineTherapy")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.ENDO_THERAPIES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//                MultipleRequests.builder()
//                        .name("subjectCountByMenopauseStatus")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.MENO_STATUS)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByMenopauseStatus")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.MENO_STATUS)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByTissueType")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.TISSUE_TYPE)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByTissueType")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.TISSUE_TYPE)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByTissueComposition")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.COMPOSITION)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByTissueComposition")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.COMPOSITION)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByFileAssociation")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.FILES)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.ASSOCIATION)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByFileAssociation")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.FILES)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.ASSOCIATION)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//
//                MultipleRequests.builder()
//                        .name("subjectCountByFileType")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.FILES)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.FILE_TYPE)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByFileType")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.FILES)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.FILE_TYPE)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//
//
//                MultipleRequests.builder()
//                        .name("armsByPrograms")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createArmProgramQuery()))
//                        .typeMapper(typeMapper.getArmProgram()).build(),
//
//                // TODO DELETE
//                MultipleRequests.builder()
//                        .name("subjectCountByLabProcedures")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.LAB_PROCEDURES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                // TODO Add Filter Query
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByLabProcedures")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createTermsAggSource(Const.BENTO_FIELDS.LAB_PROCEDURES)))
//                        .typeMapper(typeMapper.getAggregate()).build(),
//                // RANGE QUERY
//                MultipleRequests.builder()
//                        .name("filterSubjectCountByAge")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.SUBJECTS)
//                                .source(createRangeQuery()))
//                        .typeMapper(typeMapper.getRange()).build()
//
//
//        );
//        MultiSearchRequest multiRequests = new MultiSearchRequest();
//        requests.forEach(r->multiRequests.add(r.getRequest()));
//        MultiSearchResponse response = client.msearch(multiRequests, RequestOptions.DEFAULT);
//        MultiSearchResponse.Item[] responseResponses = response.getResponses();
//
//        final int[] c = {0};
//        Map<String, Object> result = new HashMap<>();
//
//        List.of(responseResponses).forEach(item->{
//            MultipleRequests multir= requests.get(c[0]);
//            result.put(multir.getName(), multir.getTypeMapper().getResolver(item.getResponse(),null));
//            c[0] += 1;
//        });
//
//        System.out.println("sss");
//    }
//


}

