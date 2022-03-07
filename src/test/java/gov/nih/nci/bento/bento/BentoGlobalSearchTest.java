package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.TypeMapper;
import gov.nih.nci.bento.service.ESService;
import gov.nih.nci.bento.utility.StrUtil;
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
//    final String _sampleId = "BENTO-BIOS-5707938";
    final String _studyId = "BENTO-STUDY-001";
    final String _tissueType = "Tumor";
    final String _sampleAnatomicSite = "Breast";
    final String _studyName = "RS 0-10, assigned endocrine therapy alone";
    final String _studyType = "Interventional Clinical Trial";
//    private final String _tissueTypeGS = "Tumor";
//    private final String _anatomicSiteGS = "Breast";

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

        String PROGRAM_ID = "NCT00310180";
        String PROGRAM_CODE = "TAILORx";
        String PROGRAM_NAME = "for the Assessment of Clinical Cancer";

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_CODE, Const.BENTO_FIELDS.PROGRAM_CODE);
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_NAME, Const.BENTO_FIELDS.PROGRAM_NAME);

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROGRAM_ID, PROGRAM_ID))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROGRAM_CODE, PROGRAM_CODE))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROGRAM_NAME, PROGRAM_NAME))
                );

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.PROGRAMS)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.PROGRAMS)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.PROGRAMS)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        assertThat(result, hasKey("TEST01"));
        List<Map<String, Object>> test01 =  (List<Map<String, Object>>) result.get("TEST01");
        assertThat(test01.size(), greaterThan(0));
        assertThat(test01.get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_ID));
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.PROGRAM_ID), is(PROGRAM_ID));

        List<Map<String, Object>> test02 =  (List<Map<String, Object>>) result.get("TEST02");
        assertThat(test02.size(), greaterThan(0));
        assertThat(test02.get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_CODE));
        assertThat((String) test02.get(0).get(Const.BENTO_FIELDS.PROGRAM_CODE), is(PROGRAM_CODE));

        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
        assertThat(test03.size(), greaterThan(0));
        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_NAME));
        assertThat((String) test03.get(0).get(Const.BENTO_FIELDS.PROGRAM_NAME), containsString(PROGRAM_NAME));
    }

    @Test
    public void searchGlobalSubject_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        // TODO AGE NEEDED TO SEARCH
        String SUBJECT_ID = "BENTO-CASE-7356713";
        String DIGNOSIS_GS = "Infiltrating Ductal & Lobular Carcinoma";
        int AGE_AT_INDEX_GS = 51;

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID); // TODO subject_id_gs
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_CODE, Const.BENTO_FIELDS.PROGRAM_CODE); // TODO programs
        returnTypes.put(Const.BENTO_FIELDS.STUDY_ACRONYM, Const.BENTO_FIELDS.STUDY_ACRONYM); // TODO study_acronym
        returnTypes.put(Const.BENTO_FIELDS.DIAGNOSES, Const.BENTO_FIELDS.DIAGNOSES);
        returnTypes.put(Const.BENTO_FIELDS.AGE_AT_INDEX, Const.BENTO_FIELDS.AGE_AT_INDEX);

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SUBJECT_ID_GS + Const.ES_UNITS.KEYWORD, List.of(SUBJECT_ID)))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.DIGNOSIS_GS, DIGNOSIS_GS))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.AGE_AT_INDEX, AGE_AT_INDEX_GS))
                );

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build()
        );


        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        assertThat(result, hasKey("TEST01"));
        List<Map<String, Object>> test01 =  (List<Map<String, Object>>) result.get("TEST01");
        assertThat(test01.size(), greaterThan(0));
        assertThat(test01.get(0), hasKey(Const.BENTO_FIELDS.SUBJECT_ID));
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.SUBJECT_ID), is(SUBJECT_ID));

        List<Map<String, Object>> test02 =  (List<Map<String, Object>>) result.get("TEST02");
        assertThat(test02.size(), greaterThan(0));
        assertThat(test02.get(0), hasKey(Const.BENTO_FIELDS.DIAGNOSES));
        assertThat((String) test02.get(0).get(Const.BENTO_FIELDS.DIAGNOSES), containsString(DIGNOSIS_GS));

        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
        assertThat(test03.size(), greaterThan(0));
        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.AGE_AT_INDEX));
        assertThat(test03.get(0).get(Const.BENTO_FIELDS.AGE_AT_INDEX), is(AGE_AT_INDEX_GS));
    }

    @Test
    public void searchGlobalSample_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        // TODO
        String SAMPLE_ID = "BENTO-BIOS-5707938";
        String TISSUE_TYPE_GS = "Tumor";
        String ANATOMIC_SITES_GS = "Breast";

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
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SAMPLE_ID_GS, List.of(SAMPLE_ID)))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE_GS, List.of(ANATOMIC_SITES_GS)))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.TISSUE_TYPE_GS, List.of(TISSUE_TYPE_GS)))
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
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.SAMPLE_ID), is(SAMPLE_ID));

        assertThat(result, hasKey("TEST02"));
        List<Map<String, Object>> test02 =  (List<Map<String, Object>>) result.get("TEST02");
        assertThat(test02.size(), greaterThan(0));
        assertThat(test02.get(0), hasKey(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE));
        assertThat((String) test02.get(0).get(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE), containsString(ANATOMIC_SITES_GS));

        assertThat(result, hasKey("TEST03"));
        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
        assertThat(test03.size(), greaterThan(0));
        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.TISSUE_TYPE));
        assertThat((String) test03.get(0).get(Const.BENTO_FIELDS.TISSUE_TYPE), containsString(TISSUE_TYPE_GS));
    }

    @Test
    public void globalSearchFiles_Test() throws IOException {
        String FILE_ID = "BENTO-FILE-1013342";
        String FILE_NAME = "_OncotypeDXqRTPCR.tx";
        // TODO Reconsider File Format Search
        String FILE_FORMAT = "txt";

        Map<String, String> returnTypes = new HashMap<>();
        // TODO field mismatch
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID); // subject_ids
        returnTypes.put(Const.BENTO_FIELDS.SAMPLE_ID, Const.BENTO_FIELDS.SAMPLE_ID); // sample_ids
        returnTypes.put(Const.BENTO_FIELDS.FILE_NAME, Const.BENTO_FIELDS.FILE_NAME);// file_names
        returnTypes.put(Const.BENTO_FIELDS.FILE_FORMAT, Const.BENTO_FIELDS.FILE_FORMAT);
        returnTypes.put(Const.BENTO_FIELDS.FILE_ID, Const.BENTO_FIELDS.FILE_ID); //file_ids
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.FILE_ID_GS, FILE_ID))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.FILE_NAME, "*" + FILE_NAME + "*" ))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_FORMAT_GS, FILE_FORMAT))
                );

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES_TEST)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES_TEST)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES_TEST)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        assertThat(result, hasKey("TEST01"));
        List<Map<String, Object>> test01 =  (List<Map<String, Object>>) result.get("TEST01");
        assertThat(test01.size(), greaterThan(0));
        assertThat(test01.get(0), hasKey(Const.BENTO_FIELDS.FILE_ID));
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.FILE_ID), containsString(FILE_ID));

        assertThat(result, hasKey("TEST02"));
        List<Map<String, Object>> test02 =  (List<Map<String, Object>>) result.get("TEST02");
        assertThat(test02.size(), greaterThan(0));
        assertThat(test02.get(0), hasKey(Const.BENTO_FIELDS.FILE_NAME));
        assertThat((String) test02.get(0).get(Const.BENTO_FIELDS.FILE_NAME), containsString(FILE_NAME));

        assertThat(result, hasKey("TEST03"));
        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
        assertThat(test03.size(), greaterThan(0));
        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.FILE_FORMAT));
        assertThat((String) test03.get(0).get(Const.BENTO_FIELDS.FILE_FORMAT), containsString(FILE_FORMAT));
    }

    @Test
    public void globalSearchModelValues_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String VALUE = "Enrollment";
        // TODO FIELD MIS-MATCH
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.NODE, Const.BENTO_FIELDS.NODE);
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY, Const.BENTO_FIELDS.PROPERTY); // subject_ids
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_TYPE, Const.BENTO_FIELDS.PROPERTY_TYPE); // sample_ids
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_REQUIRED, Const.BENTO_FIELDS.PROPERTY_REQUIRED);// file_names
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, Const.BENTO_FIELDS.PROPERTY_DESCRIPTION);
        returnTypes.put(Const.BENTO_FIELDS.VALUE, Const.BENTO_FIELDS.VALUE); //file_ids
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.VALUE, VALUE)
                        )
                // Set HighLight Builder
                ).highlighter(
                        new HighlightBuilder()
                                .field(Const.BENTO_FIELDS.VALUE)
                                .preTags("")
                                .postTags("")
                                .fragmentSize(1)
                );


        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.MODEL_VALUES)
                                .source(testBuilder01))
                        // TODO CHANGE RETURN TYPE AS MAP
                        .typeMapper(
                                typeMapper.getHighLightFragments(Const.BENTO_FIELDS.VALUE,
                                (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.PROPERTY, // TODO MISMATCH node_name
                                Const.BENTO_FIELDS.NODE, source.get(Const.BENTO_FIELDS.NODE), // TODO MISMATCH node_name
                                Const.BENTO_FIELDS.PROPERTY, source.get(Const.BENTO_FIELDS.PROPERTY), // TODO MISMATCH property_name
                                Const.BENTO_FIELDS.PROPERTY_TYPE, source.get(Const.BENTO_FIELDS.PROPERTY_TYPE),
                                Const.BENTO_FIELDS.PROPERTY_DESCRIPTION,source.get(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION),
                                Const.BENTO_FIELDS.VALUE,source.get(Const.BENTO_FIELDS.VALUE),
                                Const.BENTO_FIELDS.PROPERTY_REQUIRED,source.get(Const.BENTO_FIELDS.PROPERTY_REQUIRED))))
                        .build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        assertThat(result, hasKey("TEST01"));
        List<Map<String, Object>> test01 =  (List<Map<String, Object>>) result.get("TEST01");
        assertThat(test01.size(), greaterThan(0));
        assertThat(test01.get(0), hasKey(Const.BENTO_FIELDS.NODE));
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.NODE), notNullValue());
        assertThat((String) test01.get(0).get(Const.BENTO_FIELDS.VALUE), containsString(VALUE));
    }


    @Test
    public void globalSearchProperty_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String NODE = "program";
        String PROPERTY_TYPE = "String";
        // TODO Add Condition Statement when empty
        String PROPERTY_REQUIRED_TEXT = StrUtil.getBoolFromText("TESTETESTTEST false");
        String PROPERTY_DESCRIPTION = "Full length";

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
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROPERTY_TYPE + Const.ES_UNITS.KEYWORD, List.of(PROPERTY_TYPE)))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_REQUIRED,PROPERTY_REQUIRED_TEXT))
                );

        SearchSourceBuilder testBuilder04 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, PROPERTY_DESCRIPTION))
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
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.MODEL_PROPERTIES)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
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

        assertThat(result, hasKey("TEST03"));
        List<Map<String, Object>> test03 =  (List<Map<String, Object>>) result.get("TEST03");
        assertThat(test03.size(), greaterThan(0));
        assertThat(test03.get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_REQUIRED));
        assertThat((boolean) test03.get(0).get(Const.BENTO_FIELDS.PROPERTY_REQUIRED), is(false));

        assertThat(result, hasKey("TEST04"));
        List<Map<String, Object>> test04 =  (List<Map<String, Object>>) result.get("TEST04");
        assertThat(test04.size(), greaterThan(0));
        assertThat(test04.get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION));
        assertThat((String) test04.get(0).get(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION), containsString(PROPERTY_DESCRIPTION));
    }



    @Test
    public void globalSearchStudies_Test() throws IOException {
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
        // TODO
//        assertThat(result.get(0).get(Const.BENTO_FIELDS.PROGRAM_ID).toString(), containsString(_programId));
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
