package gov.nih.nci.bento.model.icdc;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.constants.Const.ES_PARAMS;
import gov.nih.nci.bento.constants.Const.ICDC_FIELDS;
import gov.nih.nci.bento.constants.Const.ICDC_INDEX;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.search.query.filter.AggregationFilter;
import gov.nih.nci.bento.search.query.filter.NestedFilter;
import gov.nih.nci.bento.search.result.TypeMapperService;
import gov.nih.nci.bento.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.search.yaml.filter.YamlFilterType;
import gov.nih.nci.bento.service.EsSearch;
import gov.nih.nci.bento.utility.ElasticUtil;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;


@RequiredArgsConstructor
public class ICDCEsSearch implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(ICDCEsSearch.class);
    private final EsSearch esService;

    public final TypeMapperService typeMapper;
//    private BentoQuery bentoQuery;
    private YamlQueryFactory yamlQueryFactory;

    @PostConstruct
    public void init() {
//        bentoQuery = new BentoQueryImpl(typeMapper);
        yamlQueryFactory = new YamlQueryFactory(esService, typeMapper);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                                // TODO
                                .dataFetchers(yamlQueryFactory.createICDCYamlQueries())
                                .dataFetcher("caseOverviewPaged", env ->
                                        caseOverview(CreateQueryParam(env),"asc"))
                                .dataFetcher("caseOverviewPagedDesc", env ->
                                        caseOverview(CreateQueryParam(env),"desc"))
                                .dataFetcher("sampleOverview", env ->
                                        sampleOverview(CreateQueryParam(env), "asc"))
                                .dataFetcher("sampleOverviewDesc", env ->
                                        sampleOverview(CreateQueryParam(env),"desc"))
                                .dataFetcher("fileOverview", env ->
                                        fileOverview(CreateQueryParam(env), "asc"))
                                .dataFetcher("fileOverviewDesc", env ->
                                        fileOverview(CreateQueryParam(env), "desc"))
                                // TODO BIOBANK
                                .dataFetcher("filterCaseCountByStudyParticipation_Test", env ->
                                        filterCaseCount_Test(CreateQueryParam(env),"canine_individual"))
//                                .dataFetcher("numberOfStudies", env ->
//                                        numberOfStudies(CreateQueryParam(env)))
//                                .dataFetcher("caseCountByStudyCode", env ->
//                                        caseCountByStudyCode(CreateQueryParam(env)))
                )
                .build();
    }


    private static final Map<String, String> getMapper() {

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("program", "program_acronym");
        paramMap.put("study", "clinical_study_designation");
        paramMap.put("study_type", "study_type");
//        paramMap.put("biobank", "biobank");
        paramMap.put("study_participation", "canine_individual");
        paramMap.put("breed", "breed");
        paramMap.put("diagnosis", "diagnosis");
        paramMap.put("disease_site", "disease_site");
        paramMap.put("stage_of_disease", "stage_of_disease");
        paramMap.put("response_to_treatment", "response_to_treatment");
        paramMap.put("sex", "sex");
        paramMap.put("neutered_status", "neutered_status");
        paramMap.put("sample_site", "samples_info.sample_site");
        paramMap.put("sample_type", "samples_info.sample_type");
        paramMap.put("sample_pathology", "samples_info.sample_pathology");
        paramMap.put("file_association", "files_info.association");
        paramMap.put("file_type", "files_info.file_type");
        paramMap.put("file_format", "files_info.file_format");
        return paramMap;
    }


    private List<Map<String, Object>> filterNestedCount_Test(QueryParam params, YamlFilterType filterType) throws IOException {

        Map<String, Object> args = params.getArgs();
        // TODO
        Map<String, Object> newArgs= new HashMap<>();
        Map<String, String> paramMap = getMapper();
        args.forEach((k,v)->{
            if (paramMap.containsKey(k)) {
                List<String> list = (List<String>) args.get(k);
                if (list.size() > 0) {
                    newArgs.put(paramMap.get(k), list);
                }
            }
        });
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.CASES)
                                .source(new NestedFilter(
                                        FilterParam.builder()
                                                .args(newArgs)
                                                .selectedField(filterType.getSelectedField())
                                                .nestedPath(filterType.getNestedPath())
                                                .nestedFields(filterType.getNestedFields())
                                                .build())
                                        .getSourceFilter()))
                        .typeMapper(typeMapper.getICDCNestedAggregateList()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        return (List<Map<String, Object>>) result.get("TEST01");
    }

    private List<Map<String, Object>> filterCaseCount_Test(QueryParam params, String selectedField) throws IOException {
        Map<String, String> paramMap = getMapper();
        // Get Params
        BoolQueryBuilder bool = new BoolQueryBuilder();

        Map<String, Object> args = params.getArgs();
        args.forEach((key, v)->{
            if (paramMap.containsKey(key)) {
                List<String> list = (List<String>) args.get(key);
                if (list.size() > 0) {
                    // Check nested filter
                    String nestedKey = paramMap.get(key);
                    if (nestedKey.contains(".")) {

                        String[] splitKeys= nestedKey.split("\\.");
                        bool.filter(QueryBuilders.nestedQuery(splitKeys[0],
                                QueryBuilders.termsQuery(nestedKey, (List<String>) args.get(key)),
                                ScoreMode.None));
                    } else {
                        bool.filter(
                                QueryBuilders.termsQuery(paramMap.get(key), (List<String>) args.get(key)));
                    }
                }
            }
        });
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(new SearchSourceBuilder()
                .size(0)
                .query(bool)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(selectedField).minDocCount(0)));
        return esService.elasticSend(request, typeMapper.getICDCAggregate());
    }


    private List<Map<String, Object>> caseCountByDiseaseSite(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(new AggregationFilter(FilterParam.builder()
                .args(args)
                .selectedField(ICDC_FIELDS.DISEASE_SITE)
                .build())
                .getSourceFilter());
        return esService.elasticSend(request, typeMapper.getICDCAggregate());
    }

    private List<Map<String, Object>> caseCountByStageOfDisease(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(new AggregationFilter(FilterParam.builder()
                .args(args)
                .selectedField(ICDC_FIELDS.STAGE_OF_DISEASE)
                .build())
                .getSourceFilter());
        return esService.elasticSend(request, typeMapper.getICDCAggregate());
    }
//
//
    private List<Map<String, Object>> caseCountByNeuteredStatus(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(new AggregationFilter(FilterParam.builder()
                .args(args)
                .selectedField(ICDC_FIELDS.NEUTERED_STATUS)
                .build())
                .getSourceFilter());
        return esService.elasticSend(request, typeMapper.getICDCAggregate());
    }

    private List<Map<String, Object>> caseCountByBreed(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(new AggregationFilter(FilterParam.builder()
                .args(args)
                .selectedField(ICDC_FIELDS.BREED)
                .build())
                .getSourceFilter());
        return esService.elasticSend(request, typeMapper.getICDCAggregate());
    }

    // case ids exists -> show all types of aggregation
    // others -> aggregation filter by case_ids
    private List<Map<String, Object>> caseCountByGender(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(new AggregationFilter(FilterParam.builder()
                .args(args)
                .selectedField(ICDC_FIELDS.SEX)
                .build())
                .getSourceFilter());
        return esService.elasticSend(request, typeMapper.getICDCAggregate());
    }
//
    private QueryParam CreateQueryParam(DataFetchingEnvironment env) {
        return QueryParam.builder()
                .args(env.getArguments())
                .outputType(env.getFieldType())
                .build();
    }

    // TODO Create Param Parse as a Class, caseIDS, size
    // TODO ES Service Editing
    private List<Map<String, Object>> caseOverview(QueryParam param, String sortDirection) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.CASE_ID, ids);
        searchSourceBuilder.query(
                ids.size() > 0 ? filter : QueryBuilders.matchAllQuery()
        );
        searchSourceBuilder.from(param.getTableParam().getOffSet());
        searchSourceBuilder.sort(
                // Get Default Sort Type or Pre-defined Sort Field
                args.get(ES_PARAMS.ORDER_BY).equals("") ? ICDC_FIELDS.CASE_ID : (String) args.get(ES_PARAMS.ORDER_BY),
                ElasticUtil.getSortType(sortDirection));
        searchSourceBuilder.size(param.getTableParam().getPageSize());
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(searchSourceBuilder);

        return esService.elasticSend(request, typeMapper.getList(param.getReturnTypes()));
    }

    private List<Map<String, Object>> sampleOverview(QueryParam param, String sortDirection) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.CASE_ID, ids);
        searchSourceBuilder.query(
                ids.size() > 0 ? filter : QueryBuilders.matchAllQuery()
        );
        searchSourceBuilder.from(param.getTableParam().getOffSet());
        searchSourceBuilder.sort(
                // Get Default Sort Type or Pre-defined Sort Field
                args.get(ES_PARAMS.ORDER_BY).equals("") ? ICDC_FIELDS.SAMPLE_ID : (String) args.get(ES_PARAMS.ORDER_BY),
                ElasticUtil.getSortType(sortDirection));
        searchSourceBuilder.size(param.getTableParam().getPageSize());
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.SAMPLES);
        request.source(searchSourceBuilder);
        return esService.elasticSend(request, typeMapper.getList(param.getReturnTypes()));
    }

    private List<Map<String, Object>> caseCountByDiagnosis(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Get Params
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        // Set Filter
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.DIAG_OF_CASE_CASE, ids);
        searchSourceBuilder.query(
                ids.size() == 0 ? QueryBuilders.matchAllQuery() : filter
        );
        // Set Aggregate
        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms(ES_PARAMS.TERMS_AGGS)
                .size(ES_PARAMS.AGGS_SIZE)
                .field(ICDC_FIELDS.DISEASE_TERM);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.DIAGNOSIS);
        request.source(searchSourceBuilder);

        return esService.elasticSend(request, typeMapper.getICDCAggregate());

// TODO
//        Map<String, Object> args = param.getArgs();
//        SearchRequest request = new SearchRequest();
//        request.indices(ICDC_INDEX.CASES);
//        request.source(new AggregationFilter(FilterParam.builder()
//                .args(args)
//                .selectedField(ICDC_FIELDS.DISEASE_TERM)
//                .build())
//                .getSourceFilter());
//        return esService.elasticSend(request, typeMapper.getICDCAggregate());

    }

    private List<Map<String, Object>> fileOverview(QueryParam param, String sortDirection) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // Set Filter
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.CASE_ID, ids);
        searchSourceBuilder.query(
                ids.size() > 0 ? filter : QueryBuilders.matchAllQuery()
        );
        searchSourceBuilder.from(param.getTableParam().getOffSet());
        searchSourceBuilder.sort(
                // Get Default Sort Type or Pre-defined Sort Field
                args.get(ES_PARAMS.ORDER_BY).equals("") ? ICDC_FIELDS.FILE_NAME : (String) args.get(ES_PARAMS.ORDER_BY),
                ElasticUtil.getSortType(sortDirection));
        searchSourceBuilder.size(10);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.FILES);
        request.source(searchSourceBuilder);

        return esService.elasticSend(request, typeMapper.getList(param.getReturnTypes()));
    }

}
