package gov.nih.nci.bento.model.bento;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.classes.QueryResult;
import gov.nih.nci.bento.classes.TableParam;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.model.bento.query.BentoQuery;
import gov.nih.nci.bento.model.bento.query.BentoQueryImpl;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.search.result.TypeMapperService;
import gov.nih.nci.bento.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.service.EsSearch;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@RequiredArgsConstructor
public final class BentoEsSearch implements DataFetcher {

    private static final Logger logger = LogManager.getLogger(BentoEsSearch.class);
    private final EsSearch esService;
    private final TypeMapperService typeMapper;
    private BentoQuery bentoQuery;
    private YamlQueryFactory yamlQueryFactory;

    @PostConstruct
    public void init() {
        bentoQuery = new BentoQueryImpl(typeMapper);
        yamlQueryFactory = new YamlQueryFactory(esService, typeMapper);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
//                        .dataFetcher("globalSearchProgram", env ->
//                                globalSearchProgram(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("globalSearchStudy", env ->
//                                globalSearchStudy(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("globalSearchFile", env ->
//                                globalSearchFile(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("globalSearchModel", env ->
//                                globalSearchModel(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("globalSearchAbout", env ->
//                                globalSearchAbout(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("globalSearchSample", env ->
//                                globalSearchSample(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("globalSearchSubject", env ->
//                                globalSearchSubject(esService.CreateQueryParam(env))
//                        )
                        .dataFetcher("globalSearch", env ->
                                globalSearch(esService.CreateQueryParam(env))
                        )
                        .dataFetchers(yamlQueryFactory.createYamlQueries())
//                        .dataFetcher("searchSubjects", env ->
//                                multiSearchTest(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("subjectOverview", env ->
//                                subjectOverview(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("sampleOverview", env ->
//                                sampleOverview(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("fileOverview", env ->
//                                fileOverview(esService.CreateQueryParam(env))
//                        )
//
//                        .dataFetcher("fileIDsFromList", env ->
//                                fileIDsFromList(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("filesInList", env ->
//                                filesInList(esService.CreateQueryParam(env))
//                        )
//                        .dataFetcher("findSubjectIdsInList", env ->
//                                findSubjectIdsInList(esService.CreateQueryParam(env))
//                        )
                )
                .build();
    }

//    private Object globalSearchSubject(QueryParam param) throws IOException {
//        Map<String, Object> result = new HashMap<>();
//        // Set Bool Filter
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findGlobalSearchSubject(param)
//        );
//        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
//        QueryResult subjects = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SUBJECTS);
//        List<Map<String, Object>> searchHits_Test = subjects.getSearchHits();
//        result.put("result", checkEmptySearch(param, searchHits_Test));
//        result.put("count", checkEmptySearch(param, subjects.getTotalHits()));
//        return result;
//    }
//
//    private Object globalSearchAbout(QueryParam param) throws IOException {
//        Map<String, Object> result = new HashMap<>();
//        // Set Bool Filter
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findGlobalSearchAboutPage(param)
//        );
//        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
//        QueryResult aboutPage = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_ABOUT);
//        TableParam tableParam = param.getTableParam();
//
//        List<Map<String, Object>> searchHits_Test = aboutPage.getSearchHits();
//        result.put("result", paginate(searchHits_Test, tableParam.getPageSize(), tableParam.getOffSet()));
//        result.put("count", checkEmptySearch(param, searchHits_Test.size()));
//        return result;
//    }
//
//    private Object globalSearchSample(QueryParam param) throws IOException {
//        Map<String, Object> result = new HashMap<>();
//        // Set Bool Filter
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findGlobalSearchSample(param)
//        );
//        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
//        QueryResult samples = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SAMPLE);
//
//        List<Map<String, Object>> searchHits_Test = samples.getSearchHits();
//        result.put("result", checkEmptySearch(param, searchHits_Test));
//        result.put("count", checkEmptySearch(param, samples.getTotalHits()));
//        return result;
//    }
//
//
//    private Object globalSearchModel(QueryParam param) throws IOException {
//        Map<String, Object> result = new HashMap<>();
//        // Set Bool Filter
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findGlobalSearchModel(param)
//        );
//        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
//        QueryResult model = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_MODEL);
//
//        List<Map<String, Object>> searchHits_Test = model.getSearchHits();
//        result.put("result", checkEmptySearch(param, searchHits_Test));
//        result.put("count", checkEmptySearch(param, model.getTotalHits()));
//        TableParam tableParam = param.getTableParam();
//        Set<String> combinedCategories = Set.of("result");
//        for (String category: combinedCategories) {
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> pagedCategory = paginate((List<Map<String, Object>>)result.get(category), tableParam.getPageSize(), tableParam.getOffSet());
//            result.put(category, pagedCategory);
//        }
//        return result;
//    }
//
//    private Object globalSearchFile(QueryParam param) throws IOException {
//        Map<String, Object> result = new HashMap<>();
//        // Set Bool Filter
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findGlobalSearchFile(param)
//        );
//        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
//        QueryResult files = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_FILE);
//        List<Map<String, Object>> searchHits_Test = files.getSearchHits();
//        result.put("result", checkEmptySearch(param, searchHits_Test));
//        result.put("count", checkEmptySearch(param, files.getTotalHits()));
//        return result;
//    }
//
//
//    private Object globalSearchStudy(QueryParam param) throws IOException {
//        Map<String, Object> result = new HashMap<>();
//        // Set Bool Filter
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findGlobalSearchStudy(param)
//        );
//
//        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
//        QueryResult studies = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_STUDIES);
//        List<Map<String, Object>> searchHits_Test = studies.getSearchHits();
//        result.put("result", checkEmptySearch(param, searchHits_Test));
//        result.put("count", checkEmptySearch(param, studies.getTotalHits()));
//        return result;
//    }
//
//    private Map<String, Object> globalSearchProgram(QueryParam param) throws IOException {
//        Map<String, Object> result = new HashMap<>();
//        // Set Bool Filter
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findGlobalSearchProgram(param)
//        );
//
//        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
//
//        QueryResult programs = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_PROGRAM);
//        List<Map<String, Object>> searchHits_Test = (List<Map<String, Object>>) programs.getSearchHits();
//        result.put("result", checkEmptySearch(param, searchHits_Test));
//        result.put("count", checkEmptySearch(param, programs.getTotalHits()));
//        return result;
//    }


//    private List<Map<String, Object>> subjectOverview(QueryParam param) throws IOException {
//        // Set Rest API Request
//        SearchRequest request = new SearchRequest();
//        request.indices(BENTO_INDEX.SUBJECTS);
//        request.source(
//                new TableFilter(FilterParam.builder()
//                        .args(param.getArgs())
//                        .queryParam(param)
//                        .customOrderBy(getIntCustomOrderBy(param))
//                        .defaultSortField(BENTO_FIELDS.SUBJECT_ID_NUM)
//                        .build()).getSourceFilter()
//        );
//        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
//    }

//    private List<Map<String, Object>> sampleOverview(QueryParam param) throws IOException {
//        // Set Rest API Request
//        SearchRequest request = new SearchRequest();
//        request.indices(BENTO_INDEX.SAMPLES);
//        request.source(
//                new TableFilter(FilterParam.builder()
//                        .args(param.getArgs())
//                        .queryParam(param)
//                        .customOrderBy(getIntCustomOrderBy(param))
//                        .defaultSortField(BENTO_FIELDS.SAMPLE_ID_NUM)
//                        .build()).getSourceFilter()
//        );
//        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
//    }
//
//    private List<Map<String, Object>> fileOverview(QueryParam param) throws IOException {
//        // Set Rest API Request
//        return getFileSearch(param);
//    }



//    private List<Map<String, Object>> getFileSearch(QueryParam param) throws IOException {
//        SearchRequest request = new SearchRequest();
//        request.indices(BENTO_INDEX.FILES);
//        request.source(
//                new TableFilter(FilterParam.builder()
//                        .args(param.getArgs())
//                        .queryParam(param)
//                        .customOrderBy(getIntCustomOrderBy(param))
//                        .defaultSortField(BENTO_FIELDS.FILE_NAME)
//                        .build()).getSourceFilter()
//        );
//        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
//    }

    private List<?> checkEmptySearch(QueryParam param, List<Map<String, Object>> result) {
        return param.getSearchText().equals("") ? new ArrayList<>() : result;
    }

    private int checkEmptySearch(QueryParam param, int result) {
        return param.getSearchText().equals("") ? 0 :result;
    }

    private Map<String, Object> globalSearch(QueryParam param) throws IOException {
        Map<String, Object> result = new HashMap<>();
        // Set Bool Filter
        List<MultipleRequests> requests = List.of(
                bentoQuery.findGlobalSearchSubject(param),
                bentoQuery.findGlobalSearchSample(param),
                bentoQuery.findGlobalSearchProgram(param),
                bentoQuery.findGlobalSearchStudy(param),
                bentoQuery.findGlobalSearchFile(param),
                bentoQuery.findGlobalSearchModel(param),
                bentoQuery.findGlobalSearchAboutPage(param)
        );

        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
        QueryResult subjects = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SUBJECTS);
        result.put("subjects", checkEmptySearch(param, subjects.getSearchHits()));
        result.put("subject_count", checkEmptySearch(param, subjects.getTotalHits()));

        QueryResult samples = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SAMPLE);
        result.put("samples", checkEmptySearch(param, samples.getSearchHits()));
        result.put("sample_count", checkEmptySearch(param, samples.getTotalHits()));

        QueryResult programs = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_PROGRAM);
        result.put("programs", checkEmptySearch(param, programs.getSearchHits()));
        result.put("program_count", checkEmptySearch(param, programs.getTotalHits()));

        QueryResult studies = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_STUDIES);
        result.put("studies", checkEmptySearch(param, studies.getSearchHits()));
        result.put("study_count", checkEmptySearch(param, studies.getTotalHits()));

        QueryResult files = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_FILE);
        result.put("files", checkEmptySearch(param, files.getSearchHits()));
        result.put("file_count", checkEmptySearch(param, files.getTotalHits()));

        QueryResult model = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_MODEL);
        result.put("model", checkEmptySearch(param, model.getSearchHits()));
        result.put("model_count", checkEmptySearch(param, model.getTotalHits()));

        QueryResult aboutPage = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_ABOUT);
        TableParam tableParam = param.getTableParam();
        result.put("about_count", checkEmptySearch(param, aboutPage.getSearchHits().size()));
        result.put("about_page", paginate(aboutPage.getSearchHits(), tableParam.getPageSize(), tableParam.getOffSet()));

        Set<String> combinedCategories = Set.of("model");
        for (String category: combinedCategories) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pagedCategory = paginate((List<Map<String, Object>>)result.get(category), tableParam.getPageSize(), tableParam.getOffSet());
            result.put(category, pagedCategory);
        }
        return result;
    }

    private <T> List<T> paginate(List<T> org, int pageSize, int offset) {
        List<T> result = new ArrayList<>();
        int size = org.size();
        if (offset <= size -1) {
            int end_index = offset + pageSize;
            if (end_index > size) end_index = size;
            result = org.subList(offset, end_index);
        }
        return result;
    }

//    private List<Map<String, Object>> findSubjectIdsInList(QueryParam param) throws IOException {
//        // Set Filter
//        SearchSourceBuilder builder = new DefaultFilter(FilterParam.builder()
//                        .args(param.getArgs()).build()).getSourceFilter();
//        builder.size(ES_UNITS.MAX_SIZE);
//        // Set Rest API Request
//        SearchRequest request = new SearchRequest();
//        request.indices(BENTO_INDEX.SUBJECTS);
//        request.source(builder);
//        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
//    }
//
//    private List<Map<String, Object>> filesInList(QueryParam param) throws IOException {
//        // Set Rest API Request
//        return getFileSearch(param);
//    }

//    private List<String> fileIDsFromList(QueryParam param) throws IOException {
//        SearchSourceBuilder builder = new DefaultFilter(FilterParam.builder()
//                .args(param.getArgs()).build()).getSourceFilter();
//        builder.size(ES_UNITS.MAX_SIZE);
//        // Set Rest API Request
//        SearchRequest request = new SearchRequest();
//        request.indices(BENTO_INDEX.FILES);
//        request.source(builder);
//        return esService.elasticSend(request, typeMapper.getStrList(BENTO_FIELDS.FILE_ID));
//    }

//    private Map<String, Object> multiSearchTest(QueryParam param) throws IOException {
//        Map<String, Object> args = param.getArgs();
//        List<MultipleRequests> requests = List.of(
//                bentoQuery.findNumberOfPrograms(args),
//                bentoQuery.findNumberOfStudies(args),
//                bentoQuery.findNumberOfSubjects(args),
//                bentoQuery.findNumberOfSamples(args),
//                bentoQuery.findNumberOfLabProcedures(args),
//                bentoQuery.findNumberOfFiles(args),
//                bentoQuery.findSubjectCntProgram(args),
//                bentoQuery.findFilterSubjectCntProgram(args),
//                bentoQuery.findSubjectCntStudy(args),
//                bentoQuery.findFilterSubjectCntStudy(args),
//                bentoQuery.findSubjectCntDiagnoses(args),
//                bentoQuery.findFilterSubjectCntDiagnoses(args),
//                bentoQuery.findSubjectCntRecurrence(args),
//                bentoQuery.findFilterSubjectCntRecurrence(args),
//                bentoQuery.findSubjectCntTumorSize(args),
//                bentoQuery.findFilterSubjectCntTumorSize(args),
//                bentoQuery.findSubjectCntTumorGrade(args),
//                bentoQuery.findFilterSubjectCntTumorGrade(args),
//                bentoQuery.findSubjectCntErGrade(args),
//                bentoQuery.findFilterSubjectCntErGrade(args),
//                bentoQuery.findSubjectCntPrStatus(args),
//                bentoQuery.findFilterSubjectCntPrStatus(args),
//                bentoQuery.findSubjectCntChemo(args),
//                bentoQuery.findFilterSubjectCntChemo(args),
//                bentoQuery.findSubjectCntEndoTherapy(args),
//                bentoQuery.findFilterSubjectCntEndoTherapy(args),
//                bentoQuery.findSubjectCntMenoTherapy(args),
//                bentoQuery.findFilterSubjectCntMenoTherapy(args),
//                bentoQuery.findSubjectCntTissueType(args),
//                bentoQuery.findFilterSubjectCntTissueType(args),
//                bentoQuery.findSubjectCntTissueComposition(args),
//                bentoQuery.findFilterSubjectCntTissueComposition(args),
//                bentoQuery.findSubjectCntFileAssociation(args),
//                bentoQuery.findFilterSubjectCntFileAssociation(args),
//                bentoQuery.findSubjectCntFileType(args),
//                bentoQuery.findFilterSubjectCntFileType(args),
//                bentoQuery.findNumberOfArms(args),
//                bentoQuery.findSubjectCntLabProcedures(args),
//                bentoQuery.findFilterSubjectCntLabProcedures(args),
//                bentoQuery.findFilterSubjectCntByAge(args)
//        );
//        return esService.elasticMultiSend(requests);
//    }
}