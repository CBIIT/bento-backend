package gov.nih.nci.bento.model.bento;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.classes.QueryResult;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.BENTO_INDEX;
import gov.nih.nci.bento.constants.Const.ES_UNITS;
import gov.nih.nci.bento.model.bento.query.BentoQuery;
import gov.nih.nci.bento.model.bento.query.BentoQueryImpl;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.EsSearch;
import gov.nih.nci.bento.utility.StrUtil;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@RequiredArgsConstructor
public class BentoEsSearch implements DataFetcher {

    private static final Logger logger = LogManager.getLogger(BentoEsSearch.class);
    private final EsSearch esService;
    public final TypeMapperImpl typeMapper;

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchSubjects", env ->
                                multiSearchTest(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("subjectOverview", env ->
                                subjectOverview_Test(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("sampleOverview", env ->
                                sampleOverviewTest(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("fileOverview", env ->
                                fileOverviewTest(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("globalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                        .dataFetcher("fileIDsFromList", env ->
                                fileIDsFromListTest(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("filesInList", env ->
                                filesInListTest(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("findSubjectIdsInList", env ->
                                findSubjectIdsInListTest(esService.CreateQueryParam(env))
                        )
                )
                .build();
    }

    private List<Map<String, Object>> subjectOverview_Test(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(
                new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        .defaultSortField(BENTO_FIELDS.SUBJECT_ID_NUM)
                        .build()).getSourceFilter()
        );
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

    private List<Map<String, Object>> sampleOverviewTest(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SAMPLES);
        request.source(
                new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        .defaultSortField(BENTO_FIELDS.SAMPLE_ID_NUM)
                        .build()).getSourceFilter()
        );
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

    private List<Map<String, Object>> fileOverviewTest(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(
                new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        .defaultSortField(BENTO_FIELDS.FILE_NAME+ ES_UNITS.KEYWORD)
                        .build()).getSourceFilter()
        );
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

    private Map<String, Object> globalSearch(Map<String, Object> params) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String input = (String) params.get("input");
        int size = (int) params.get(Const.ES_PARAMS.PAGE_SIZE);
        int offset = (int) params.get(Const.ES_PARAMS.OFFSET);
        Set<String> combinedCategories = Set.of("model") ;

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(
                        addConditionalQuery(
                        new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.SUBJECT_ID_GS + ES_UNITS.KEYWORD, input))
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.DIGNOSIS_GS + Const.ES_UNITS.KEYWORD, "*" + input+ "*")),
                        // Set Conditional Integer Query
                        QueryBuilders.termQuery(Const.BENTO_FIELDS.AGE_AT_INDEX,StrUtil.getIntText(input)))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.SAMPLE_ID_GS + ES_UNITS.KEYWORD, input))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE_GS + ES_UNITS.KEYWORD, input))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.TISSUE_TYPE_GS + ES_UNITS.KEYWORD, input))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROGRAM_ID + ES_UNITS.KEYWORD, input))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROGRAM_CODE + Const.ES_UNITS.KEYWORD, input))
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROGRAM_NAME, "*" + input + "*"))
                );

        SearchSourceBuilder testBuilder04 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_ID + Const.ES_UNITS.KEYWORD, input))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_NAME  + Const.ES_UNITS.KEYWORD, input))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_TYPE  + Const.ES_UNITS.KEYWORD, input))
                );


        SearchSourceBuilder testBuilder05 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_ID_GS + Const.ES_UNITS.KEYWORD, input))
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.FILE_NAME, "*" + input + "*" ))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_FORMAT_GS, input))
                );

        SearchSourceBuilder testBuilder06 = new SearchSourceBuilder()
                .size(ES_UNITS.MAX_SIZE)
                .from(0)
                .sort(Const.BENTO_FIELDS.PROGRAM_KW + ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(
                        addConditionalQuery(
                                new BoolQueryBuilder()
                                .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.VALUE + Const.ES_UNITS.KEYWORD, "*" + input + "*"))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_NAME + ES_UNITS.KEYWORD, input))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_TYPE + ES_UNITS.KEYWORD, input))
                                .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION + Const.ES_UNITS.KEYWORD, "*" + input + "*"))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.NODE_NAME + ES_UNITS.KEYWORD, input)),
                                // Set Conditional Bool Query
                                QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_REQUIRED,StrUtil.getBoolText(input)))
                ).highlighter(
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


        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                                Const.BENTO_FIELDS.TYPE,
                                Const.BENTO_FIELDS.PROGRAM_ID,
                                Const.BENTO_FIELDS.SUBJECT_ID,
                                BENTO_FIELDS.PROGRAM,
                                Const.BENTO_FIELDS.STUDY_ACRONYM,
                                Const.BENTO_FIELDS.DIAGNOSES,
                                Const.BENTO_FIELDS.AGE_AT_INDEX
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SAMPLES)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                                Const.BENTO_FIELDS.TYPE,
                                Const.BENTO_FIELDS.PROGRAM_ID,
                                Const.BENTO_FIELDS.SUBJECT_ID,
                                BENTO_FIELDS.SAMPLE_ID,
                                Const.BENTO_FIELDS.DIAGNOSES,
                                Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE,
                                Const.BENTO_FIELDS.TISSUE_TYPE
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.PROGRAMS)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                                Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.PROGRAM_CODE,
                                BENTO_FIELDS.PROGRAM_ID,
                                BENTO_FIELDS.PROGRAM_NAME
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST04")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.STUDIES)
                                .source(testBuilder04))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                                Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.PROGRAM_ID,
                                BENTO_FIELDS.STUDY_ID,
                                BENTO_FIELDS.STUDY_TYPE,
                                BENTO_FIELDS.STUDY_CODE,
                                BENTO_FIELDS.STUDY_NAME
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST05")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(testBuilder05))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                                Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.PROGRAM_ID,
                                BENTO_FIELDS.SUBJECT_ID,
                                BENTO_FIELDS.SAMPLE_ID,
                                BENTO_FIELDS.FILE_NAME,
                                BENTO_FIELDS.FILE_FORMAT,
                                BENTO_FIELDS.FILE_ID
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST06")
                        .request(new SearchRequest()
                                .indices(new String[]{BENTO_INDEX.MODEL_PROPERTIES, BENTO_INDEX.MODEL_VALUES, BENTO_INDEX.MODEL_NODES})
                                .source(testBuilder06))
                        .typeMapper(typeMapper.getMapWithHighlightedFields(Set.of(
                                Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.NODE_NAME,
                                BENTO_FIELDS.PROPERTY_NAME,
                                BENTO_FIELDS.PROPERTY_DESCRIPTION,
                                BENTO_FIELDS.PROPERTY_TYPE,
                                BENTO_FIELDS.PROPERTY_REQUIRED,
                                BENTO_FIELDS.VALUE
                        ))).build()
        );

        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) multiResult.get("TEST01");
        result.put("subjects", test01Result.getSearchHits());
        result.put("subject_count", test01Result.getTotalHits());

        QueryResult test02Result = (QueryResult) multiResult.get("TEST02");
        result.put("samples", test02Result.getSearchHits());
        result.put("sample_count", test02Result.getTotalHits());

        QueryResult test03Result = (QueryResult) multiResult.get("TEST03");
        result.put("programs", test03Result.getSearchHits());
        result.put("program_count", test03Result.getTotalHits());

        QueryResult test04Result = (QueryResult) multiResult.get("TEST04");
        result.put("studies", test04Result.getSearchHits());
        result.put("study_count", test04Result.getTotalHits());

        QueryResult test05Result = (QueryResult) multiResult.get("TEST05");
        result.put("files", test05Result.getSearchHits());
        result.put("file_count", test05Result.getTotalHits());

        QueryResult test06Result = (QueryResult) multiResult.get("TEST06");
        result.put("model", test06Result.getSearchHits());
        result.put("model_count", test06Result.getTotalHits());

        List<Map<String, Object>> about_results = searchAboutPageTest(input);
        int about_count = about_results.size();
        result.put("about_count", about_count);
        result.put("about_page", paginate(about_results, size, offset));
        // TODO
        for (String category: combinedCategories) {
            List<Object> pagedCategory = paginate((List)result.get(category), size, offset);
            result.put(category, pagedCategory);
        }

        return result;
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

    private List paginate(List org, int pageSize, int offset) {
        List<Object> result = new ArrayList<>();
        int size = org.size();
        if (offset <= size -1) {
            int end_index = offset + pageSize;
            if (end_index > size) {
                end_index = size;
            }
            result = org.subList(offset, end_index);
        }
        return result;
    }

    private List<Map<String, Object>> searchAboutPageTest(String input) throws IOException {

        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        bool.should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.CONTENT_PARAGRAPH, input));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(Const.BENTO_INDEX.ABOUT);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(Const.BENTO_FIELDS.CONTENT_PARAGRAPH);
        highlightBuilder.preTags(ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        highlightBuilder.postTags(ES_UNITS.GS_HIGHLIGHT_DELIMITER);
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

        return result;
    }

    private List<Map<String, Object>> findSubjectIdsInListTest(QueryParam param) throws IOException {
        // Set Filter
        SearchSourceBuilder builder = new DefaultFilter(FilterParam.builder()
                        .args(param.getArgs()).build()).getSourceFilter();
        builder.size(ES_UNITS.MAX_SIZE);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(builder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

    private List<Map<String, Object>> filesInListTest(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(new TableFilter(FilterParam.builder()
                .args(param.getArgs())
                .queryParam(param)
                .defaultSortField(BENTO_FIELDS.FILE_NAME+ ES_UNITS.KEYWORD)
                .build()).getSourceFilter());
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

    private List<String> fileIDsFromListTest(QueryParam param) throws IOException {
        SearchSourceBuilder builder = new DefaultFilter(FilterParam.builder()
                .args(param.getArgs()).build()).getSourceFilter();
        builder.size(ES_UNITS.MAX_SIZE);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(builder);
        List<String>  result = esService.elasticSend(null, request, typeMapper.getStrList(BENTO_FIELDS.FILE_ID));
        return result;
    }

    // TODO
    private Map<String, Object> multiSearchTest(QueryParam param) throws IOException {
        BentoQuery bentoQuery = new BentoQueryImpl(typeMapper);
        Map<String, Object> args = param.getArgs();
        List<MultipleRequests> requests = List.of(
                bentoQuery.findNumberOfPrograms(),
                bentoQuery.findNumberOfStudies(),
                bentoQuery.findNumberOfSubjects(args),
                bentoQuery.findNumberOfSamples(args),
                bentoQuery.findNumberOfLabProcedures(args),
                bentoQuery.findNumberOfFiles(args),
                bentoQuery.findSubjectCntProgram(args),
                bentoQuery.findFilterSubjectCntProgram(args),
                bentoQuery.findSubjectCntStudy(args),
                bentoQuery.findFilterSubjectCntStudy(args),
                bentoQuery.findSubjectCntDiagnoses(args),
                bentoQuery.findFilterSubjectCntDiagnoses(args),
                bentoQuery.findSubjectCntRecurrence(args),
                bentoQuery.findFilterSubjectCntRecurrence(args),
                bentoQuery.findSubjectCntTumorSize(args),
                bentoQuery.findFilterSubjectCntTumorSize(args),
                bentoQuery.findSubjectCntTumorGrade(args),
                bentoQuery.findFilterSubjectCntTumorGrade(args),
                bentoQuery.findSubjectCntErGrade(args),
                bentoQuery.findFilterSubjectCntErGrade(args),
                bentoQuery.findSubjectCntPrStatus(args),
                bentoQuery.findFilterSubjectCntPrStatus(args),
                bentoQuery.findSubjectCntChemo(args),
                bentoQuery.findFilterSubjectCntChemo(args),
                bentoQuery.findSubjectCntEndoTherapy(args),
                bentoQuery.findFilterSubjectCntEndoTherapy(args),
                bentoQuery.findSubjectCntMenoTherapy(args),
                bentoQuery.findFilterSubjectCntMenoTherapy(args),
                bentoQuery.findSubjectCntTissueType(args),
                bentoQuery.findFilterSubjectCntTissueType(args),
                bentoQuery.findSubjectCntTissueComposition(args),
                bentoQuery.findFilterSubjectCntTissueComposition(args),
                bentoQuery.findSubjectCntFileAssociation(args),
                bentoQuery.findFilterSubjectCntFileAssociation(args),
                bentoQuery.findSubjectCntFileType(args),
                bentoQuery.findFilterSubjectCntFileType(args),
                bentoQuery.findNumberOfArms(args),
                bentoQuery.findSubjectCntLabProcedures(args),
                bentoQuery.findFilterSubjectCntLabProcedures(args),
                bentoQuery.findFilterSubjectCntByAge(args)
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        return result;
    }
}
