package gov.nih.nci.bento.model;

import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.constants.Const.ES_PARAMS;
import gov.nih.nci.bento.constants.Const.ICDC_FIELDS;
import gov.nih.nci.bento.constants.Const.ICDC_INDEX;
import gov.nih.nci.bento.service.ESServiceImpl;
import gov.nih.nci.bento.utility.ElasticUtil;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;


@RequiredArgsConstructor
public class IcdcEsSearch implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(IcdcEsSearch.class);
    @Autowired
    ESServiceImpl esService;

    public final TypeMapperImpl typeMapper;

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
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
                                .dataFetcher("caseCountByDiagnosis", env ->
                                        caseCountByDiagnosis(CreateQueryParam(env)))
                                .dataFetcher("caseCountByGender", env ->
                                        caseCountByGender(CreateQueryParam(env)))
                                .dataFetcher("caseCountByBreed", env ->
                                        caseCountByBreed(CreateQueryParam(env)))
                                .dataFetcher("caseCountByNeuteredStatus", env ->
                                        caseCountByNeuteredStatus(CreateQueryParam(env)))
                                .dataFetcher("caseCountByStageOfDisease", env ->
                                        caseCountByStageOfDisease(CreateQueryParam(env)))
                                .dataFetcher("caseCountByDiseaseSite", env ->
                                        caseCountByDiseaseSite(CreateQueryParam(env)))
//                                .dataFetcher("caseCountByStudyCode", env ->
//                                        caseCountByStudyCode(CreateQueryParam(env)))
                )
                .build();
    }

    private List<Map<String, Object>> caseCountByDiseaseSite(QueryParam params) throws IOException {

        Map<String, Object> args = params.getArgs();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // Get Params
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        // Set Filter
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.DIAG_CASE_OF_CASE, ids);

        if (ids.size() > 0) builder.query(filter);
        // Set Aggregate
        builder.aggregation(ElasticUtil.getTermsAggregation(ICDC_FIELDS.PRIMARY_DISEASE_SITE));
        builder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.DIAGNOSIS);
        request.source(builder);
        List<Map<String, Object>> result = esService.elasticSend(params.getReturnTypes(), request, typeMapper.getAggregate());
        return result;
    }

    private List<Map<String, Object>> caseCountByStageOfDisease(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // Get Params
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        // Set Filter
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.DIAG_CASE_OF_CASE, ids);
        if (ids.size() > 0) builder.query(filter);
        // Set Aggregate
        builder.aggregation(ElasticUtil.getTermsAggregation(ICDC_FIELDS.STAGE_OF_DISEASE));
        builder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.DIAGNOSIS);
        request.source(builder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getAggregate());
        return result;
    }
//
//
    private List<Map<String, Object>> caseCountByNeuteredStatus(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // Get Params
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        // Set Filter
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.NEUTERED_INDICATOR, ids);
        if (ids.size() > 0) builder.query(filter);
        // Set Aggregate
        TermsAggregationBuilder aggregation = ElasticUtil.getTermsAggregation(ICDC_FIELDS.DEMOG_OF_CASE_CASE);
        builder.aggregation(aggregation);
        builder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.DEMOGRAPHIC);
        request.source(builder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getAggregate());
        return result;
    }

    private List<Map<String, Object>> caseCountByBreed(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // Get Params
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        // Set Filter
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.DEMOG_OF_CASE_CASE, ids);
        if (ids.size() > 0) builder.query(filter);
        // Set Aggregate
        builder.aggregation(ElasticUtil.getTermsAggregation(ICDC_FIELDS.BREED));
        builder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.DEMOGRAPHIC);
        request.source(builder);

        List<Map<String, Object>> result = esService.elasticSend(null, request, typeMapper.getAggregate());
        return result;
    }

    // case ids exists -> show all types of aggregation
    // others -> aggregation filter by case_ids
    private List<Map<String, Object>> caseCountByGender(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // Get Params
        List<String> ids = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        // Set Filter
        QueryBuilder filter = QueryBuilders.termsQuery(ICDC_FIELDS.DEMOG_OF_CASE_CASE, ids);
        if (ids.size() > 0) builder.query(filter);
        // Set Aggregate
        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms(ES_PARAMS.TERMS_AGGS)
                .size(ES_PARAMS.AGGS_SIZE)
                .field(ICDC_FIELDS.SEX);
        builder.aggregation(aggregation);
        builder.size(0);

        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.DEMOGRAPHIC);
        request.source(builder);

        List<Map<String, Object>> result = esService.elasticSend(null, request, typeMapper.getAggregate());
        return result;
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
        searchSourceBuilder.from(param.getOffSet());
        searchSourceBuilder.sort(
                // Get Default Sort Type or Pre-defined Sort Field
                args.get(ES_PARAMS.ORDER_BY).equals("") ? ICDC_FIELDS.CASE_ID : (String) args.get(ES_PARAMS.ORDER_BY),
                ElasticUtil.getSortType(sortDirection));
        searchSourceBuilder.size(param.getPageSize());
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.CASES);
        request.source(searchSourceBuilder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
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
        searchSourceBuilder.from(param.getOffSet());
        searchSourceBuilder.sort(
                // Get Default Sort Type or Pre-defined Sort Field
                args.get(ES_PARAMS.ORDER_BY).equals("") ? ICDC_FIELDS.SAMPLE_ID : (String) args.get(ES_PARAMS.ORDER_BY),
                ElasticUtil.getSortType(sortDirection));
        searchSourceBuilder.size(param.getPageSize());
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.SAMPLES);
        request.source(searchSourceBuilder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
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

        List<Map<String, Object>> result = esService.elasticSend(null, request, typeMapper.getAggregate());
        return result;
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
        searchSourceBuilder.from(param.getOffSet());
        searchSourceBuilder.sort(
                // Get Default Sort Type or Pre-defined Sort Field
                args.get(ES_PARAMS.ORDER_BY).equals("") ? ICDC_FIELDS.FILE_NAME : (String) args.get(ES_PARAMS.ORDER_BY),
                ElasticUtil.getSortType(sortDirection));
        searchSourceBuilder.size(param.getPageSize());
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(ICDC_INDEX.FILES);
        request.source(searchSourceBuilder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

}
