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
import gov.nih.nci.bento.search.query.filter.DefaultFilter;
import gov.nih.nci.bento.search.query.filter.TableFilter;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.EsSearch;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

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
                                subjectOverview(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("sampleOverview", env ->
                                sampleOverview(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("fileOverview", env ->
                                fileOverview(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("globalSearch", env ->
                                globalSearch(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("fileIDsFromList", env ->
                                fileIDsFromList(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("filesInList", env ->
                                filesInList(esService.CreateQueryParam(env))
                        )
                        .dataFetcher("findSubjectIdsInList", env ->
                                findSubjectIdsInList(esService.CreateQueryParam(env))
                        )
                )
                .build();
    }

    private List<Map<String, Object>> subjectOverview(QueryParam param) throws IOException {
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

    private List<Map<String, Object>> sampleOverview(QueryParam param) throws IOException {
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

    private List<Map<String, Object>> fileOverview(QueryParam param) throws IOException {
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

    private Map<String, Object> globalSearch(QueryParam param) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String input = param.getSearchText();
        int size = param.getTableParam().getPageSize();
        int offset = param.getTableParam().getOffSet();
        Set<String> combinedCategories = Set.of("model");

        // Set Bool Filter
        BentoQuery bentoQuery = new BentoQueryImpl(typeMapper);
        List<MultipleRequests> requests = List.of(
                bentoQuery.findGlobalSearchSubject(param),
                bentoQuery.findGlobalSearchSample(param),
                bentoQuery.findGlobalSearchProgram(param),
                bentoQuery.findGlobalSearchStudy(param),
                bentoQuery.findGlobalSearchFile(param),
                bentoQuery.findGlobalSearchFile(param),
                bentoQuery.findGlobalSearchModel(param)
        );

        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SUBJECTS);
        result.put("subjects", test01Result.getSearchHits());
        result.put("subject_count", test01Result.getTotalHits());

        QueryResult test02Result = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SAMPLE);
        result.put("samples", test02Result.getSearchHits());
        result.put("sample_count", test02Result.getTotalHits());

        QueryResult test03Result = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_PROGRAM);
        result.put("programs", test03Result.getSearchHits());
        result.put("program_count", test03Result.getTotalHits());

        QueryResult test04Result = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_STUDIES);
        result.put("studies", test04Result.getSearchHits());
        result.put("study_count", test04Result.getTotalHits());

        QueryResult test05Result = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_FILE);
        result.put("files", test05Result.getSearchHits());
        result.put("file_count", test05Result.getTotalHits());

        QueryResult test06Result = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_MODEL);
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

    private List<Map<String, Object>> findSubjectIdsInList(QueryParam param) throws IOException {
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

    private List<Map<String, Object>> filesInList(QueryParam param) throws IOException {
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

    private List<String> fileIDsFromList(QueryParam param) throws IOException {
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
