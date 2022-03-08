package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryResult;
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
import org.elasticsearch.search.sort.SortOrder;
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

    @Test
    public void getGlobal_AboutPage_Test() throws IOException {
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        bool.should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.CONTENT_PARAGRAPH, "open source"));
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
        String PROGRAM_ID = "nct";
        String PROGRAM_CODE = "TAILORx";
        String PROGRAM_NAME = "Assessment";

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_CODE, Const.BENTO_FIELDS.PROGRAM_CODE);
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_NAME, Const.BENTO_FIELDS.PROGRAM_NAME);

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + Const.ES_UNITS.KEYWORD)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROGRAM_ID, "*" + PROGRAM_ID + "*" ))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + Const.ES_UNITS.KEYWORD)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROGRAM_CODE, PROGRAM_CODE))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + Const.ES_UNITS.KEYWORD)
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
        QueryResult test01 = (QueryResult) result.get("TEST01");
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(test01.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_ID));
        assertThat(((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROGRAM_ID)).toLowerCase(), containsString(PROGRAM_ID));

        QueryResult test02 = (QueryResult) result.get("TEST02");
        assertThat(test02.getSearchHits().size(), greaterThan(0));
        assertThat(test02.getTotalHits(), greaterThan(0));
        assertThat(test02.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_CODE));
        assertThat((String) test02.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROGRAM_CODE), is(PROGRAM_CODE));

        QueryResult test03 = (QueryResult) result.get("TEST03");
        assertThat(test03.getSearchHits().size(), greaterThan(0));
        assertThat(test03.getTotalHits(), greaterThan(0));
        assertThat(test03.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.PROGRAM_NAME));
        assertThat(((String) test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROGRAM_NAME)).toLowerCase(), containsString(PROGRAM_NAME.toLowerCase()));
    }

    @Test
    public void searchGlobalSubject_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        // TODO AGE NEEDED TO SEARCH
        String SUBJECT_ID = "bento";
        String DIGNOSIS_GS = "Infiltrating Ductal";
        // TODO ADD Conditional Statement
        String AGE_AT_INDEX_GS = "tumor 49";

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE);
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID); // TODO subject_id_gs
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM, Const.BENTO_FIELDS.PROGRAM); // TODO programs
        returnTypes.put(Const.BENTO_FIELDS.STUDY_ACRONYM, Const.BENTO_FIELDS.STUDY_ACRONYM); // TODO study_acronym
        returnTypes.put(Const.BENTO_FIELDS.DIAGNOSES, Const.BENTO_FIELDS.DIAGNOSES);
        returnTypes.put(Const.BENTO_FIELDS.AGE_AT_INDEX, Const.BENTO_FIELDS.AGE_AT_INDEX);

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.SUBJECT_ID_GS, SUBJECT_ID))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.DIGNOSIS_GS, DIGNOSIS_GS))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.AGE_AT_INDEX, StrUtil.getIntText(AGE_AT_INDEX_GS)))
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
        QueryResult test01 = (QueryResult) result.get("TEST01");
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(test01.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.SUBJECT_ID));
        assertThat(((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.SUBJECT_ID)).toLowerCase(), containsString(SUBJECT_ID));

        QueryResult test02 = (QueryResult) result.get("TEST02");
        assertThat(test02.getSearchHits().size(), greaterThan(0));
        assertThat(test02.getTotalHits(), greaterThan(0));
        assertThat(test02.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.DIAGNOSES));
        assertThat((String) test02.getSearchHits().get(0).get(Const.BENTO_FIELDS.DIAGNOSES), containsString(DIGNOSIS_GS));

        QueryResult test03 = (QueryResult) result.get("TEST03");
        assertThat(test03.getSearchHits().size(), greaterThan(0));
        assertThat(test03.getTotalHits(), greaterThan(0));
        assertThat(test03.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.AGE));
        assertThat(test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.AGE), is(49));
    }

    @Test
    public void searchGlobalSample_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String SAMPLE_ID = "bento";
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
                .sort(Const.BENTO_FIELDS.SAMPLE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.SAMPLE_ID_GS, SAMPLE_ID))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SAMPLE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE_GS + Const.ES_UNITS.KEYWORD, List.of(ANATOMIC_SITES_GS)))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SAMPLE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.TISSUE_TYPE_GS + Const.ES_UNITS.KEYWORD, List.of(TISSUE_TYPE_GS)))
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

        QueryResult test01 = (QueryResult) result.get("TEST01");
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(test01.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.SAMPLE_ID));
        assertThat(((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.SAMPLE_ID)).toLowerCase(), containsString(SAMPLE_ID));

        QueryResult test02 = (QueryResult) result.get("TEST02");
        assertThat(test02.getSearchHits().size(), greaterThan(0));
        assertThat(test02.getTotalHits(), greaterThan(0));
        assertThat(test02.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE));
        assertThat((String) test02.getSearchHits().get(0).get(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE), containsString(ANATOMIC_SITES_GS));

        QueryResult test03 = (QueryResult) result.get("TEST03");
        assertThat(test03.getSearchHits().size(), greaterThan(0));
        assertThat(test03.getTotalHits(), greaterThan(0));
        assertThat(test03.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.TISSUE_TYPE));
        assertThat((String) test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.TISSUE_TYPE), containsString(TISSUE_TYPE_GS));
    }

    @Test
    public void globalSearchFiles_Test() throws IOException {
        String FILE_ID = "bento";
        String FILE_NAME = "_OncotypeDXqRTPCR.tx";
        String FILE_FORMAT = "txt";

        Map<String, String> returnTypes = new HashMap<>();
        // TODO field mismatch
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID); // TODO subject_ids
        returnTypes.put(Const.BENTO_FIELDS.SAMPLE_ID, Const.BENTO_FIELDS.SAMPLE_ID); // TODO  sample_ids
        returnTypes.put(Const.BENTO_FIELDS.FILE_NAME, Const.BENTO_FIELDS.FILE_NAME);// TODO file_names
        returnTypes.put(Const.BENTO_FIELDS.FILE_FORMAT, Const.BENTO_FIELDS.FILE_FORMAT);
        returnTypes.put(Const.BENTO_FIELDS.FILE_ID, Const.BENTO_FIELDS.FILE_ID); // TODO file_ids
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.FILE_ID_GS, FILE_ID))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.FILE_NAME, "*" + FILE_NAME + "*" ))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
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

        QueryResult test01 = (QueryResult) result.get("TEST01");
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.FILE_ID)).toLowerCase(), containsString(FILE_ID.toLowerCase()));

        QueryResult test02 = (QueryResult) result.get("TEST02");
        assertThat(test02.getSearchHits().size(), greaterThan(0));
        assertThat(test02.getTotalHits(), greaterThan(0));
        assertThat((String) test02.getSearchHits().get(0).get(Const.BENTO_FIELDS.FILE_NAME), containsString(FILE_NAME));

        QueryResult test03 = (QueryResult) result.get("TEST03");
        assertThat(test03.getSearchHits().size(), greaterThan(0));
        assertThat(test03.getTotalHits(), greaterThan(0));
        assertThat((String) test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.FILE_FORMAT), containsString(FILE_FORMAT));
    }

    @Test
    public void globalSearchModelValues_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String VALUE = "Enrollment";
        // TODO FIELD MIS-MATCH
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.NODE_NAME, Const.BENTO_FIELDS.NODE_NAME);
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_NAME, Const.BENTO_FIELDS.PROPERTY_NAME); // subject_ids
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_TYPE, Const.BENTO_FIELDS.PROPERTY_TYPE); // sample_ids
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_REQUIRED, Const.BENTO_FIELDS.PROPERTY_REQUIRED);// file_names
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, Const.BENTO_FIELDS.PROPERTY_DESCRIPTION);
        returnTypes.put(Const.BENTO_FIELDS.VALUE, Const.BENTO_FIELDS.VALUE); //file_ids
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.VALUE + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
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
                        .typeMapper(
                                typeMapper.getMapWithHighlightedFields(returnTypes))
                        .build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));

        QueryResult test01 =  (QueryResult) result.get("TEST01");
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(test01.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.NODE_NAME));
        assertThat((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.NODE_NAME), notNullValue());
        assertThat((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.VALUE), containsString(VALUE));
        assertThat((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.VALUE), containsString(VALUE));
        assertThat((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.HIGHLIGHT), containsString(VALUE));
    }


    @Test
    public void globalSearchProperty_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String PROPERTY = "institution_id";
        String PROPERTY_TYPE = "String";
        String PROPERTY_REQUIRED_TEXT = StrUtil.getBoolText("TESTETESTTEST false");
        String PROPERTY_DESCRIPTION = "Full length";

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.NODE_NAME, Const.BENTO_FIELDS.NODE_NAME);// TODO node_name
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_NAME, Const.BENTO_FIELDS.PROPERTY_NAME); // TODO property_name
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_TYPE, Const.BENTO_FIELDS.PROPERTY_TYPE);
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_REQUIRED, Const.BENTO_FIELDS.PROPERTY_REQUIRED);
        returnTypes.put(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, Const.BENTO_FIELDS.PROPERTY_DESCRIPTION);
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .sort(Const.BENTO_FIELDS.PROGRAM_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.PROPERTY_NAME, List.of(PROPERTY)))
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

        SearchSourceBuilder testBuilder05 = new SearchSourceBuilder()
                .sort(Const.BENTO_FIELDS.PROGRAM_KW+ Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .size(1)
                .sort(Const.BENTO_FIELDS.PROPERTY_NAME + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, PROPERTY_DESCRIPTION))
                ).highlighter(
                        new HighlightBuilder()
                                .field(Const.BENTO_FIELDS.PROPERTY_NAME)
                                .field(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION)
                                .field(Const.BENTO_FIELDS.PROPERTY_TYPE)
                                .field(Const.BENTO_FIELDS.PROPERTY_REQUIRED)
                                .preTags("")
                                .postTags("")
                                .fragmentSize(1)
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
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST05")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.MODEL_PROPERTIES)
                                .source(testBuilder05))
                        .typeMapper(typeMapper.getMapWithHighlightedFields(returnTypes)).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        QueryResult test01 = (QueryResult) result.get("TEST01");
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_NAME));
        assertThat((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_NAME), is(PROPERTY));

        QueryResult test02 = (QueryResult) result.get("TEST02");
        assertThat(test02.getTotalHits(), greaterThan(0));
        assertThat(test02.getSearchHits().size(), greaterThan(0));
        assertThat(test02.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_TYPE));
        assertThat((String) test02.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_TYPE), containsString(PROPERTY_TYPE));

        QueryResult test03 = (QueryResult) result.get("TEST03");
        assertThat(test03.getTotalHits(), greaterThan(0));
        assertThat(test03.getSearchHits().size(), greaterThan(0));
        assertThat(test03.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_REQUIRED));
        assertThat((boolean) test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_REQUIRED), is(false));

        QueryResult test04 = (QueryResult) result.get("TEST04");
        assertThat(test04.getTotalHits(), greaterThan(0));
        assertThat(test04.getSearchHits().size(), greaterThan(0));
        assertThat(test04.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION));
        assertThat((String) test04.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION), containsString(PROPERTY_DESCRIPTION));

        QueryResult test05 = (QueryResult) result.get("TEST05");
        assertThat(test05.getTotalHits(), greaterThan(0));
        assertThat(test05.getSearchHits().size(), greaterThan(0));
        assertThat(test05.getSearchHits().get(0).get(Const.BENTO_FIELDS.HIGHLIGHT), is(notNullValue()));

    }

    @Test
    public void globalSearchNode_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String NODE = "program";
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.NODE_NAME, Const.BENTO_FIELDS.NODE_NAME);// TODO node_name
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.NODE_NAME + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                                .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.NODE_NAME, NODE)
                                )
                // Set HighLight Builder
                ).highlighter(
                        new HighlightBuilder()
                                .field(Const.BENTO_FIELDS.NODE_NAME)
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
                        .typeMapper(
                                typeMapper.getMapWithHighlightedFields(returnTypes))
                        .build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));
        QueryResult test01 =  (QueryResult) result.get("TEST01");
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.NODE_NAME));
        assertThat((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.NODE_NAME), notNullValue());
    }

    @Test
    public void globalStudiesSearch_Test() throws IOException {
        // Set Builder
        final String STUDY_ID = "bento";
        final String STUDY_NAME = "endocrine";
        final String STUDY_TYPE = "clinical trial";
        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_ID, Const.BENTO_FIELDS.STUDY_ID);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_TYPE, Const.BENTO_FIELDS.STUDY_TYPE);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_CODE, Const.BENTO_FIELDS.STUDY_CODE);
        returnTypes.put(Const.BENTO_FIELDS.STUDY_NAME, Const.BENTO_FIELDS.STUDY_NAME);

        SearchSourceBuilder studyIdBuilder = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.STUDY_ID, STUDY_ID))
                );
        SearchSourceBuilder studyNameBuilder = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.STUDY_NAME, STUDY_NAME))
                );

        SearchSourceBuilder studyTypeBuilder = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.STUDY_TYPE, STUDY_TYPE))
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
        QueryResult test01 =  (QueryResult) result.get("TEST01");
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.STUDY_ID));
        assertThat(((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.STUDY_ID)).toLowerCase(), containsString(STUDY_ID));

        QueryResult test02 =  (QueryResult) result.get("TEST02");
        assertThat(test02.getTotalHits(), greaterThan(0));
        assertThat(test02.getSearchHits().size(), greaterThan(0));
        assertThat(test02.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.STUDY_NAME));
        assertThat(((String) test02.getSearchHits().get(0).get(Const.BENTO_FIELDS.STUDY_NAME)).toLowerCase(), containsString(STUDY_NAME));

        QueryResult test03 =  (QueryResult) result.get("TEST03");
        assertThat(test03.getTotalHits(), greaterThan(0));
        assertThat(test03.getSearchHits().size(), greaterThan(0));
        assertThat(test03.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.STUDY_TYPE));
        assertThat(((String) test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.STUDY_TYPE)).toLowerCase(), containsString(STUDY_TYPE));
    }

}
