package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.TypeMapper;
import gov.nih.nci.bento.service.ESService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
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
public class BentoGlobalSearchTest {

    @Autowired
    ESService esService;

    @Autowired
    TypeMapper typeMapper;

    @Autowired
    ConfigurationDAO config;
    // Set Research IDs
    List<String> _subjectIds = List.of("BENTO-CASE-9971167", "BENTO-CASE-7356713");
    List<String> _fileNames = List.of("10099_OncotypeDXqRTPCR.txt", "10097_OncotypeDXqRTPCR.txt");
    final String _programNameText = "TAILORx";
    final String _programId = "NCT00310180";
    final String _sampleId = "BENTO-BIOS-5707938";
    final String _studyId = "BENTO-STUDY-001";
    final String _tissueType = "Tumor";
    final String _sampleAnatomicSite = "Breast";
    final String _studyName = "RS 0-10, assigned endocrine therapy alone";
    final String _studyType = "Interventional Clinical Trial";
    private final String _tissueTypeGS = "Tumor";
    private final String _anatomicSiteGS = "Breast";

    @Test
    public void getGlobal_AboutPage_Test() throws IOException {
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        bool.should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.CONTENT_PARAGRAPH, List.of("bento")));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(Const.BENTO_INDEX.ABOUT);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(Const.BENTO_FIELDS.CONTENT_PARAGRAPH);
        highlightBuilder.preTags(Const.ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        highlightBuilder.postTags(Const.ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        builder.highlighter(highlightBuilder);
        request.source(builder);

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PAGE, Const.BENTO_FIELDS.PAGE);
        returnTypes.put(Const.BENTO_FIELDS.TITLE, Const.BENTO_FIELDS.TITLE);
        returnTypes.put(Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE);
        returnTypes.put(Const.BENTO_FIELDS.TEXT, Const.BENTO_FIELDS.TEXT);

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request,
                typeMapper.getHighLightFragments(Const.BENTO_FIELDS.CONTENT_PARAGRAPH,
                        (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.ABOUT,
                                Const.BENTO_FIELDS.PAGE, source.get(Const.BENTO_FIELDS.PAGE),
                                Const.BENTO_FIELDS.TITLE,source.get(Const.BENTO_FIELDS.TITLE),
                                Const.BENTO_FIELDS.TEXT, text)));
        assertThat(result.size(), greaterThan(0));
        assertThat(result.get(0), hasKey(Const.BENTO_FIELDS.TYPE));
        assertThat(result.get(0).get(Const.BENTO_FIELDS.TYPE), is(Const.BENTO_FIELDS.ABOUT));
        assertThat(result.get(0).get(Const.BENTO_FIELDS.TEXT).toString(), containsString(Const.ES_UNITS.GS_HIGHLIGHT_DELIMITER));
    }

    @Test
    // TODO Change to Multiple Search
    public void searchGlobalCase_Test() throws IOException {
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        SearchSourceBuilder builder = new SearchSourceBuilder();

        bool.should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROGRAM_ID, List.of(_programNameText, _programId)));
//        bool.should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROGRAM_CODE, List.of(_programNameText, _programId)));
//        bool.should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROGRAM_NAME, List.of(_programNameText, _programId)));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(Const.BENTO_INDEX.PROGRAMS);
        request.source(builder);

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_CODE, Const.BENTO_FIELDS.PROGRAM_CODE);
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_NAME, Const.BENTO_FIELDS.PROGRAM_NAME);

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size(), greaterThan(0));
        assertThat(result.get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_CODE));
        assertThat(result.get(0).get(Const.BENTO_FIELDS.PROGRAM_CODE), is(notNullValue()));
        assertThat(result.get(0).get(Const.BENTO_FIELDS.PROGRAM_ID).toString(), containsString(_programId));
    }


    @Test
    public void searchGlobalSample_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        // TODO
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID); // TODO subject_ids
        returnTypes.put(Const.BENTO_FIELDS.SAMPLE_ID, Const.BENTO_FIELDS.SAMPLE_ID); // TODO sample_ids
        returnTypes.put(Const.BENTO_FIELDS.DIAGNOSES, Const.BENTO_FIELDS.DIAGNOSES); // TODO diagnoses
        returnTypes.put(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE, Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE);
        returnTypes.put(Const.BENTO_FIELDS.TISSUE_TYPE, Const.BENTO_FIELDS.TISSUE_TYPE);
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SAMPLE_ID, List.of(_sampleId)))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE_GS, List.of(_anatomicSiteGS)))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.TISSUE_TYPE_GS, List.of(_tissueTypeGS)))
                );

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SAMPLES)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SAMPLES)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SAMPLES)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        assertThat(result, hasKey("TEST01"));
        List<Map<String, Object>> test01 =  (List<Map<String, Object>>) result.get("TEST01");
        assertThat(test01.size(), greaterThan(0));
        assertThat(test01.get(0), hasKey(Const.BENTO_FIELDS.SAMPLE_ID));
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.SAMPLE_ID), is(_sampleId));

        assertThat(result, hasKey("TEST02"));
        List<Map<String, Object>> test02 =  (List<Map<String, Object>>) result.get("TEST02");
        assertThat(test02.size(), greaterThan(0));
        assertThat(test02.get(0), hasKey(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE));
        assertThat((String) test02.get(0).get(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE), containsString(_anatomicSiteGS));

        assertThat(result, hasKey("TEST03"));
        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
        assertThat(test03.size(), greaterThan(0));
        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.TISSUE_TYPE));
        assertThat((String) test03.get(0).get(Const.BENTO_FIELDS.TISSUE_TYPE), containsString(_tissueTypeGS));
    }

    @Test
    public void globalSearchProperty_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        // TODO
//        searchCategories.add(Map.of(
//                GS_END_POINT, PROPERTIES_END_POINT,
//                GS_COUNT_ENDPOINT, PROPERTIES_COUNT_END_POINT,
//                GS_COUNT_RESULT_FIELD, "model_count",
//                GS_RESULT_FIELD, "model",
//                GS_SEARCH_FIELD, List.of("property", "property_description", "property_type", "property_required"),
//                GS_SORT_FIELD, "property_kw",
//                GS_COLLECT_FIELDS, new String[][]{
//                        new String[]{"node_name", "node"},
//                        new String[]{"property_name", "property"},
//                        new String[]{"property_type", "property_type"},
//                        new String[]{"property_required", "property_required"},
//                        new String[]{"property_description", "property_description"}
//                },
//                GS_HIGHLIGHT_FIELDS, new String[][] {
//                        new String[]{"highlight", "property"},
//                        new String[]{"highlight", "property_description"},
//                        new String[]{"highlight", "property_type"},
//                        new String[]{"highlight", "property_required"}
//                },
//                GS_CATEGORY_TYPE, "property"
//        ));
//
        String NODE = "program";
        // TODO DELETED???
        String PROPERTY_TYPE = "String";
//        String PROPERTY_REQUIRED = "true";
        String PROPERTY_DESCRIPTION = "Full length (multiple sentence) version of the program description.";

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.NODE, Const.BENTO_FIELDS.NODE);// TODO node_name
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY, Const.BENTO_FIELDS.PROPERTY); // TODO property_name
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_TYPE, Const.BENTO_FIELDS.PROPERTY_TYPE);
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_REQUIRED, Const.BENTO_FIELDS.PROPERTY_REQUIRED);
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, Const.BENTO_FIELDS.PROPERTY_DESCRIPTION);
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.NODE, List.of(NODE)))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROPERTY_TYPE, List.of(PROPERTY_TYPE)))
                );

//        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
//                .size(1)
//                .query(new BoolQueryBuilder()
//                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROPERTY_REQUIRED, List.of(PROPERTY_REQUIRED)))
//                );
        // TODO Change to Match Query
        SearchSourceBuilder testBuilder04 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, List.of(PROPERTY_DESCRIPTION)))
                );

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.MODEL_PROPERTIES)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.MODEL_PROPERTIES)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
//                MultipleRequests.builder()
//                        .name("TEST03")
//                        .request(new SearchRequest()
//                                .indices(Const.BENTO_INDEX.MODEL_PROPERTIES)
//                                .source(testBuilder03))
//                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),

                MultipleRequests.builder()
                        .name("TEST04")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.MODEL_PROPERTIES)
                                .source(testBuilder04))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        assertThat(result, hasKey("TEST01"));
        List<Map<String, Object>> test01 =  (List<Map<String, Object>>) result.get("TEST01");
        assertThat(test01.size(), greaterThan(0));
        assertThat(test01.get(0), hasKey(Const.BENTO_FIELDS.NODE));
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.NODE), is(NODE));

        assertThat(result, hasKey("TEST02"));
        List<Map<String, Object>> test02 =  (List<Map<String, Object>>) result.get("TEST02");
        assertThat(test02.size(), greaterThan(0));
        assertThat(test02.get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_TYPE));
        assertThat((String) test02.get(0).get(Const.BENTO_FIELDS.PROPERTY_TYPE), containsString(PROPERTY_TYPE));

//        assertThat(result, hasKey("TEST03"));
//        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
//        assertThat(test03.size(), greaterThan(0));
//        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_REQUIRED));
//        assertThat((String) test03.get(0).get(Const.BENTO_FIELDS.PROPERTY_REQUIRED), containsString(PROPERTY_REQUIRED));

        assertThat(result, hasKey("TEST04"));
        List<Map<String, Object>> test04 =  (List<Map<String, Object>>) result.get("TEST04");
        assertThat(test04.size(), greaterThan(0));
        assertThat(test04.get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION));
        assertThat((String) test04.get(0).get(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION), containsString(PROPERTY_DESCRIPTION));
    }



    @Test
    public void lobalSearchStudies_Test() throws IOException {
        // Set Bool Filter
        BoolQueryBuilder bool = new BoolQueryBuilder()
                .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.STUDY_ID, List.of(_studyId)));
        QueryBuilders.termsQuery(Const.BENTO_FIELDS.STUDY_ID, List.of(_studyId));
//        QueryBuilders.termsQuery(Const.BENTO_FIELDS.STUDY_NAME, List.of(_studyId));
//        QueryBuilders.termsQuery(Const.BENTO_FIELDS.STUDY_TYPE, List.of(_studyId));

        // Set Builder
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(bool);
        // Set Request
        SearchRequest request = new SearchRequest();
        request.indices(Const.BENTO_INDEX.STUDIES);
        request.source(builder);

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_ID, Const.BENTO_FIELDS.STUDY_ID);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_TYPE, Const.BENTO_FIELDS.STUDY_TYPE);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_CODE, Const.BENTO_FIELDS.STUDY_CODE);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_NAME, Const.BENTO_FIELDS.STUDY_NAME);

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request, typeMapper.getDefault());
        assertThat(result.size(), greaterThan(0));
        assertThat(result.get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_ID));
        assertThat(result.get(0).get(Const.BENTO_FIELDS.PROGRAM_ID).toString(), containsString(_programId));
    }

    @Test
    public void globalStudiesSearch_Test() throws IOException {
        // Set Builder
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_ID, Const.BENTO_FIELDS.STUDY_ID);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_TYPE, Const.BENTO_FIELDS.STUDY_TYPE);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_CODE, Const.BENTO_FIELDS.STUDY_CODE);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_NAME, Const.BENTO_FIELDS.STUDY_NAME);

        SearchSourceBuilder studyIdBuilder = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.STUDY_ID, List.of(_studyId)))
                );
        SearchSourceBuilder studyNameBuilder = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.STUDY_NAME, List.of(_studyName)))
                );

        SearchSourceBuilder studyTypeBuilder = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.STUDY_TYPE, List.of(_studyType)))
                );


        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.STUDIES)
                                .source(studyIdBuilder))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.STUDIES)
                                .source(studyNameBuilder))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.STUDIES)
                                .source(studyTypeBuilder))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        assertThat(result, hasKey("TEST01"));
        List<Map<String, Object>> test01 =  (List<Map<String, Object>>) result.get("TEST01");
        assertThat(test01.size(), greaterThan(0));
        assertThat(test01.get(0), hasKey(Const.BENTO_FIELDS.STUDY_ID));
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.STUDY_ID), is(_studyId));

        assertThat(result, hasKey("TEST02"));
        List<Map<String, Object>> test02 =  (List<Map<String, Object>>) result.get("TEST02");
        assertThat(test02.size(), greaterThan(0));
        assertThat(test02.get(0), hasKey(Const.BENTO_FIELDS.STUDY_NAME));
        assertThat((String) test02.get(0).get(Const.BENTO_FIELDS.STUDY_NAME), containsString(_studyName));

        assertThat(result, hasKey("TEST03"));
        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
        assertThat(test03.size(), greaterThan(0));
        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.STUDY_TYPE));
        assertThat((String) test03.get(0).get(Const.BENTO_FIELDS.STUDY_TYPE), containsString(_studyType));
    }

}
