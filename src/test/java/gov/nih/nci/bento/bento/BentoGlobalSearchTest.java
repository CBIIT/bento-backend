package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryResult;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.ESServiceImpl;
import gov.nih.nci.bento.utility.StrUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
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
public class BentoGlobalSearchTest {

    @Autowired
    ESServiceImpl esService;

    @Autowired
    TypeMapperImpl typeMapper;

    @Autowired
    ConfigurationDAO config;

    @Test
    public void getGlobal_AboutPage_Test() throws IOException {
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        bool.should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.CONTENT_PARAGRAPH, "source framework"));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(Const.BENTO_INDEX.ABOUT);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(Const.BENTO_FIELDS.CONTENT_PARAGRAPH);
        highlightBuilder.preTags(Const.ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        highlightBuilder.postTags(Const.ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        builder.highlighter(highlightBuilder);
        request.source(builder);

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.PAGE);
        returnTypes.add(Const.BENTO_FIELDS.TITLE);
        returnTypes.add(Const.BENTO_FIELDS.TYPE);
        returnTypes.add(Const.BENTO_FIELDS.TEXT);

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

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM_CODE);
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM_NAME);

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + Const.ES_UNITS.KEYWORD)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROGRAM_ID, "*" + PROGRAM_ID + "*" ))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + Const.ES_UNITS.KEYWORD)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROGRAM_CODE, PROGRAM_CODE))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + Const.ES_UNITS.KEYWORD)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROGRAM_NAME, "*" + PROGRAM_NAME + "*"))
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
        String SUBJECT_ID = "bento";
        String DIGNOSIS_GS = "Infiltrating Ductal";
        String AGE_AT_INDEX_GS = "tumor 49";

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.TYPE);
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.add(Const.BENTO_FIELDS.SUBJECT_ID); // TODO subject_id_gs
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM); // TODO programs
        returnTypes.add(Const.BENTO_FIELDS.STUDY_ACRONYM); // TODO study_acronym
        returnTypes.add(Const.BENTO_FIELDS.DIAGNOSES);
        returnTypes.add(Const.BENTO_FIELDS.AGE_AT_INDEX);

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(Const.BENTO_FIELDS.SUBJECT_ID_GS, SUBJECT_ID))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.DIGNOSIS_GS + Const.ES_UNITS.KEYWORD, "*" + DIGNOSIS_GS + "*"))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(Const.BENTO_FIELDS.AGE_AT_INDEX, StrUtil.getIntText(AGE_AT_INDEX_GS)))
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
        assertThat(test03.getSearchHits().get(0), hasKey(Const.BENTO_FIELDS.AGE_AT_INDEX));
        assertThat(test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.AGE_AT_INDEX), is(49));
    }

    @Test
    public void searchGlobalSample_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String SAMPLE_ID = "BENTO-BIOS-7356713";
        String TISSUE_TYPE_GS = "Tumor";
        String ANATOMIC_SITES_GS = "Breast";

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.add(Const.BENTO_FIELDS.SUBJECT_ID); // TODO subject_ids
        returnTypes.add(Const.BENTO_FIELDS.SAMPLE_ID); // TODO sample_ids
        returnTypes.add(Const.BENTO_FIELDS.DIAGNOSES); // TODO diagnoses
        returnTypes.add(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE);
        returnTypes.add(Const.BENTO_FIELDS.TISSUE_TYPE);
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SAMPLE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(Const.BENTO_FIELDS.SAMPLE_ID_GS + Const.ES_UNITS.KEYWORD, SAMPLE_ID))
                );
        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SAMPLE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE_GS + Const.ES_UNITS.KEYWORD, List.of(ANATOMIC_SITES_GS)))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.SAMPLE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termsQuery(Const.BENTO_FIELDS.TISSUE_TYPE_GS + Const.ES_UNITS.KEYWORD, List.of(TISSUE_TYPE_GS)))
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
        assertThat(((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.SAMPLE_ID)), containsString(SAMPLE_ID));

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
        String FILE_ID = "BENTO-FILE-1013342";
        String FILE_NAME = "_OncotypeDXqRTPCR.tx";
        String FILE_FORMAT = "txt";

        Set<String> returnTypes = new HashSet<>();
        // TODO field mismatch
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.add(Const.BENTO_FIELDS.SUBJECT_ID); // TODO subject_ids
        returnTypes.add(Const.BENTO_FIELDS.SAMPLE_ID); // TODO  sample_ids
        returnTypes.add(Const.BENTO_FIELDS.FILE_NAME);// TODO file_names
        returnTypes.add(Const.BENTO_FIELDS.FILE_FORMAT);
        returnTypes.add(Const.BENTO_FIELDS.FILE_ID); // TODO file_ids
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_ID_GS + Const.ES_UNITS.KEYWORD, FILE_ID))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.FILE_NAME, "*" + FILE_NAME + "*" ))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_FORMAT_GS, FILE_FORMAT))
                );

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.FILES)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(returnTypes)).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        assertThat(result.size(), equalTo(requests.size()));

        QueryResult test01 = (QueryResult) result.get("TEST01");
        assertThat(test01.getSearchHits().size(), greaterThan(0));
        assertThat(test01.getTotalHits(), greaterThan(0));
        assertThat(((String) test01.getSearchHits().get(0).get(Const.BENTO_FIELDS.FILE_ID)), containsString(FILE_ID));

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
    public void globalSearchMultipleModelValues_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String PROPERTY_NAME = "institution_id";
        String PROPERTY_TYPE = "String";
        String PROPERTY_REQUIRED_TEXT = StrUtil.getBoolText("TESTETESTTEST false");
        String PROPERTY_DESCRIPTION = "Full length";
        String VALUE = "First";

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.TYPE);// TODO node_name
        returnTypes.add(Const.BENTO_FIELDS.NODE_NAME);// TODO node_name
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_NAME); // TODO property_name
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_TYPE);
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_REQUIRED);
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION);
        returnTypes.add(Const.BENTO_FIELDS.VALUE);
        // Set Bool Filter


        String[] indices = {Const.BENTO_INDEX.MODEL_PROPERTIES, Const.BENTO_INDEX.MODEL_VALUES, Const.BENTO_INDEX.MODEL_NODES};

        SearchSourceBuilder test01 = createMultipleModelGlobalSearch_Test(PROPERTY_NAME);
        Map<String, Object> test01Result = esService.elasticMultiSend(createMultipleRequests(indices, test01, returnTypes));
        QueryResult queryResult01 =  (QueryResult) test01Result.get("TEST");
        assertThat(queryResult01.getSearchHits().size(), greaterThan(0));
        assertThat(((String) queryResult01.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_NAME)).toLowerCase(),
                containsString(PROPERTY_NAME.toLowerCase()));

        SearchSourceBuilder test02 = createMultipleModelGlobalSearch_Test(PROPERTY_TYPE);
        Map<String, Object> test02Result = esService.elasticMultiSend(createMultipleRequests(indices, test02, returnTypes));
        QueryResult queryResult02 =  (QueryResult) test02Result.get("TEST");
        assertThat(queryResult02.getSearchHits().size(), greaterThan(0));
        assertThat(((String) queryResult02.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_TYPE)).toLowerCase(),
                containsString(PROPERTY_TYPE.toLowerCase()));

        SearchSourceBuilder test03 = createMultipleModelGlobalSearch_Test(VALUE);
        Map<String, Object> test03Result = esService.elasticMultiSend(createMultipleRequests(indices, test03, returnTypes));
        QueryResult queryResult03 =  (QueryResult) test03Result.get("TEST");
        assertThat(queryResult03.getSearchHits().size(), greaterThan(0));
        assertThat(((String) queryResult03.getSearchHits().get(0).get(Const.BENTO_FIELDS.VALUE)).toLowerCase(),
                is(notNullValue()));


        SearchSourceBuilder test04 = createMultipleModelGlobalSearch_Test(PROPERTY_REQUIRED_TEXT);
        Map<String, Object> test04Result = esService.elasticMultiSend(createMultipleRequests(indices, test04, returnTypes));
        QueryResult queryResult04 =  (QueryResult) test04Result.get("TEST");
        assertThat(queryResult04.getSearchHits().size(), greaterThan(0));
        assertThat(((boolean) queryResult04.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_REQUIRED)),
                is(notNullValue()));

        // Property Description
        SearchSourceBuilder test05 = createMultipleModelGlobalSearch_Test(PROPERTY_DESCRIPTION);
        Map<String, Object> test05Result = esService.elasticMultiSend(createMultipleRequests(indices, test05, returnTypes));
        QueryResult queryResult05 =  (QueryResult) test05Result.get("TEST");
        assertThat(queryResult05.getSearchHits().size(), greaterThan(0));
        assertThat(((String) queryResult05.getSearchHits().get(0).get(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION)),
                containsString(PROPERTY_DESCRIPTION));

    }

    private List<MultipleRequests> createMultipleRequests(String[] indices, SearchSourceBuilder builder, Set<String> returnTypes) {
        return List.of(
                MultipleRequests.builder()
                        .name("TEST")
                        .request(new SearchRequest()
                                .indices(indices)
                                .source(builder))
                        .typeMapper(typeMapper.getMapWithHighlightedFields(returnTypes)).build()
        );
    }

    private SearchSourceBuilder createMultipleModelGlobalSearch_Test(String text) {
        BoolQueryBuilder builder = addConditionalQuery(
                        new BoolQueryBuilder()
                                .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.VALUE + Const.ES_UNITS.KEYWORD, "*" + text + "*"))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_NAME + Const.ES_UNITS.KEYWORD, text))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_TYPE + Const.ES_UNITS.KEYWORD, text))
                                .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION + Const.ES_UNITS.KEYWORD, "*" + text + "*"))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.NODE_NAME + Const.ES_UNITS.KEYWORD, text)),
                                // Set Conditional Bool Query
                                QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_REQUIRED,StrUtil.getBoolText(text)));
        return new SearchSourceBuilder()
                // CAN'T SET SORT
                .size(1)
                .query(builder)
                .highlighter(
                        new HighlightBuilder()
                                // Index model_properties
                                .field(Const.BENTO_FIELDS.PROPERTY_NAME)
                                .field(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION)
                                .field(Const.BENTO_FIELDS.PROPERTY_TYPE)
                                .field(Const.BENTO_FIELDS.PROPERTY_REQUIRED)
                                // Index model_values
                                .field(Const.BENTO_FIELDS.VALUE)
                                // Index model_nodes
                                .field(Const.BENTO_FIELDS.NODE_NAME)
                                .preTags("")
                                .postTags("")
                                .fragmentSize(1)
                );
    }
    // Add Conditional Query
    private BoolQueryBuilder addConditionalQuery(BoolQueryBuilder builder, QueryBuilder... query) {
        List<QueryBuilder> builders = Arrays.asList(query);
        builders.forEach(q->{
            if (q.getName().equals("match")) {
                MatchQueryBuilder matchQuery = getQuery(q);
                if (!matchQuery.value().equals("")) builder.should(q);
            } else if (q.getName().equals("term")) {
                TermQueryBuilder termQuery = getQuery(q);
                if (!termQuery.value().equals("")) builder.should(q);
            }
        });
        return builder;
    }

    @SuppressWarnings("unchecked")
    private <T> T getQuery(QueryBuilder q) {
        String queryType = q.getName();
        return (T) q.queryName(queryType);
    }

    @Test
    public void globalSearchModelValues_Test() throws IOException {
        // Set Builder(Mis-Field Name Match)
        String VALUE = "Enrollment";
        // TODO FIELD MIS-MATCH
        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.NODE_NAME);
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_NAME); // subject_ids
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_TYPE); // sample_ids
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_REQUIRED);// file_names
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION);
        returnTypes.add(Const.BENTO_FIELDS.VALUE); //file_ids
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
        String PROPERTY_REQUIRED_TEXT = StrUtil.getBoolText("false");
        String PROPERTY_DESCRIPTION = "Full length";

        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.NODE_NAME);// TODO node_name
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_NAME); // TODO property_name
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_TYPE);
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_REQUIRED);
        returnTypes.add(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION);
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
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_REQUIRED,PROPERTY_REQUIRED_TEXT))
                );

        SearchSourceBuilder testBuilder04 = new SearchSourceBuilder()
                .size(1)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION + Const.ES_UNITS.KEYWORD, "*" + PROPERTY_DESCRIPTION + "*"))
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
        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.NODE_NAME);// TODO node_name
        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.NODE_NAME + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.NODE_NAME, NODE)
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
        final String STUDY_TYPE = "Interventional Clinical";
        Set<String> returnTypes = new HashSet<>();
        returnTypes.add(Const.BENTO_FIELDS.PROGRAM_ID);
        returnTypes.add(Const.BENTO_FIELDS.STUDY_ID);
        returnTypes.add(Const.BENTO_FIELDS.STUDY_TYPE);
        returnTypes.add(Const.BENTO_FIELDS.STUDY_CODE);
        returnTypes.add(Const.BENTO_FIELDS.STUDY_NAME);

        SearchSourceBuilder studyIdBuilder = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_ID, STUDY_ID))
                );
        SearchSourceBuilder studyNameBuilder = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_NAME, STUDY_NAME))
                );

        SearchSourceBuilder studyTypeBuilder = new SearchSourceBuilder()
                .size(1)
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.STUDY_TYPE + Const.ES_UNITS.KEYWORD, "*" + STUDY_TYPE + "*" ))
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
        assertThat(((String) test03.getSearchHits().get(0).get(Const.BENTO_FIELDS.STUDY_TYPE)), containsString(STUDY_TYPE));
    }

}
