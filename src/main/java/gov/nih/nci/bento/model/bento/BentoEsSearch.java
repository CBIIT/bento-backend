package gov.nih.nci.bento.model.bento;

import gov.nih.nci.bento.classes.*;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.BENTO_INDEX;
import gov.nih.nci.bento.constants.Const.ES_UNITS;
import gov.nih.nci.bento.model.bento.query.BentoQuery;
import gov.nih.nci.bento.model.bento.query.BentoQueryImpl;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.result.TypeMapper;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.EsSearch;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@RequiredArgsConstructor
public final class BentoEsSearch implements DataFetcher {

    private final EsSearch esService;
    private final TypeMapperImpl typeMapper;
    private BentoQuery bentoQuery;

    @PostConstruct
    public void init() {
        bentoQuery = new BentoQueryImpl(typeMapper);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("globalSearch", env ->
                                globalSearch(esService.CreateQueryParam(env))
                        )
                        .dataFetchers(singleQueryYaml_Test())
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

    public Map<String, graphql.schema.DataFetcher> singleQueryYaml_Test() throws IOException {
        Yaml yaml = new Yaml(new Constructor(SingleQuery.class));
        SingleQuery singleQuery = yaml.load(new ClassPathResource("single_query.yml").getInputStream());
        Map<String, graphql.schema.DataFetcher> map = new HashMap<>();
        singleQuery.getQuery().forEach(q->{
            map.put(q.getName(), env -> getYamlQuery(esService.CreateQueryParam(env), q));
        });

        Yaml groupYaml = new Yaml(new Constructor(GroupQuery.class));
        GroupQuery groupQuery = groupYaml.load(new ClassPathResource("group_query.yml").getInputStream());
        groupQuery.getGroups().forEach(group->{
            String queryName = group.getName();
            map.put(queryName, env -> createGroupQuery(group, esService.CreateQueryParam(env)));
        });

        return map;
    }

    private Object createGroupQuery(GroupQuery.Group group,QueryParam param) throws IOException {
        List<MultipleRequests> requests = new ArrayList<>();
        group.getQuery().forEach(q->{
            MultipleRequests multipleRequest = MultipleRequests.builder()
                    .name(q.getName())
                    .request(new SearchRequest()
                            .indices(q.getIndex())
                            .source(getSourceBuilder(param, q)))
                    .typeMapper(getTypeMapper(param, q)).build();
            requests.add(multipleRequest);

        });
        return esService.elasticMultiSend(requests);
    }

    private Object getYamlQuery(QueryParam param, YamlQuery query) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(query.getIndex());
        request.source(getSourceBuilder(param, query));
        return esService.elasticSend(request, getTypeMapper(param, query));
    }

    private SearchSourceBuilder getSourceBuilder(QueryParam param, YamlQuery query) {
        // Set Arguments
        YamlFilterType filterType = query.getFilterType();
        if (filterType.getType().equals("aggregation")) {
            return new AggregationFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .selectedField(filterType.getSelectedField())
                            .isExcludeFilter(filterType.isFilter())
                            .build())
                    .getSourceFilter();

        } else if (filterType.getType().equals("table")) {
            return new TableFilter(FilterParam.builder()
                    .args(param.getArgs())
                    .queryParam(param)
                    .customOrderBy(getIntCustomOrderBy(param))
                    .defaultSortField(filterType.getSelectedField())
                    .build()).getSourceFilter();
        } else if (filterType.getType().equals("number_of_docs")) {
            return new SearchCountFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .build())
                    .getSourceFilter();
        } else if (filterType.getType().equals("range")) {
            return new RangeFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .selectedField(filterType.getSelectedField())
                            .isExcludeFilter(true)
                            .build())
                    .getSourceFilter();
        } else if (filterType.getType().equals("sub_aggregation")) {
            return new SubAggregationFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .selectedField(filterType.getSelectedField())
                            .subAggSelectedField(filterType.getSubAggSelectedField())
                            .build())
                    .getSourceFilter();
        } else if (filterType.getType().equals("default")) {
            return new DefaultFilter(FilterParam.builder()
                    .args(param.getArgs()).build()).getSourceFilter();
        }
        throw new IllegalArgumentException();
    }

    private TypeMapper getTypeMapper(QueryParam param, YamlQuery query) {
        // Set Result Type
        if (query.getResultType().equals("default")) {
            return typeMapper.getDefault(param.getReturnTypes());
        } else if (query.getResultType().equals("aggregation")) {
            return typeMapper.getAggregate();
        } else if (query.getResultType().equals("int_total_aggregation")) {
            return typeMapper.getAggregateTotalCnt();
        }  else if (query.getResultType().equals("range")) {
            return typeMapper.getRange();
        }  else if (query.getResultType().equals("arm_program")) {
            return typeMapper.getArmProgram();
        }  else if (query.getResultType().equals("int_total_count")) {
            return typeMapper.getIntTotal();
        }  else if (query.getResultType().equals("str_list")) {
            return typeMapper.getStrList(query.getFilterType().getSelectedField());
        }
        throw new IllegalArgumentException();
    }
    private List<Map<String, Object>> subjectOverview(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(
                new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        .customOrderBy(getIntCustomOrderBy(param))
                        .defaultSortField(BENTO_FIELDS.SUBJECT_ID_NUM)
                        .build()).getSourceFilter()
        );
        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
    }

    private List<Map<String, Object>> sampleOverview(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SAMPLES);
        request.source(
                new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        .customOrderBy(getIntCustomOrderBy(param))
                        .defaultSortField(BENTO_FIELDS.SAMPLE_ID_NUM)
                        .build()).getSourceFilter()
        );
        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
    }

    private List<Map<String, Object>> fileOverview(QueryParam param) throws IOException {
        // Set Rest API Request
        return getFileSearch(param);
    }

    private String getIntCustomOrderBy(QueryParam param) {
        String orderKey = param.getTableParam().getOrderBy();
        Map<String, String> customKeyMap = Map.of(
                BENTO_FIELDS.SUBJECT_ID, BENTO_FIELDS.SUBJECT_ID_NUM,
                BENTO_FIELDS.SAMPLE_ID, BENTO_FIELDS.SAMPLE_ID_NUM);
        return customKeyMap.getOrDefault(orderKey, "");
    }

    private List<Map<String, Object>> getFileSearch(QueryParam param) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(
                new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        .customOrderBy(getIntCustomOrderBy(param))
                        .defaultSortField(BENTO_FIELDS.FILE_NAME)
                        .build()).getSourceFilter()
        );
        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
    }

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

    private List<Map<String, Object>> findSubjectIdsInList(QueryParam param) throws IOException {
        // Set Filter
        SearchSourceBuilder builder = new DefaultFilter(FilterParam.builder()
                        .args(param.getArgs()).build()).getSourceFilter();
        builder.size(ES_UNITS.MAX_SIZE);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(builder);
        return esService.elasticSend(request, typeMapper.getDefault(param.getReturnTypes()));
    }

    private List<Map<String, Object>> filesInList(QueryParam param) throws IOException {
        // Set Rest API Request
        return getFileSearch(param);
    }

    private List<String> fileIDsFromList(QueryParam param) throws IOException {
        SearchSourceBuilder builder = new DefaultFilter(FilterParam.builder()
                .args(param.getArgs()).build()).getSourceFilter();
        builder.size(ES_UNITS.MAX_SIZE);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(builder);
        return esService.elasticSend(request, typeMapper.getStrList(BENTO_FIELDS.FILE_ID));
    }

    private Map<String, Object> multiSearchTest(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        List<MultipleRequests> requests = List.of(
                bentoQuery.findNumberOfPrograms(args),
                bentoQuery.findNumberOfStudies(args),
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
        return esService.elasticMultiSend(requests);
    }
}
