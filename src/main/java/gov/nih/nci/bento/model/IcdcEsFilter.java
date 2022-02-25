package gov.nih.nci.bento.model;

import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;


@RequiredArgsConstructor
public class IcdcEsFilter implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(IcdcEsFilter.class);
    @Autowired
    ESService esService;

    public final TypeMapper typeMapper;

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
//                                .dataFetcher("caseOverviewPaged", env ->
//                                        caseOverview(CreateQueryParam(env),"asc"))
//                                .dataFetcher("caseOverviewPagedDesc", env ->
//                                        caseOverview(CreateQueryParam(env),"desc"))
//                                .dataFetcher("sampleOverview", env ->
//                                        sampleOverview(CreateQueryParam(env), "asc"))
//                                .dataFetcher("sampleOverviewDesc", env ->
//                                        sampleOverview(CreateQueryParam(env),"desc"))
//                                .dataFetcher("fileOverview", env ->
//                                        fileOverview(CreateQueryParam(env), "asc"))
//                                .dataFetcher("fileOverviewDesc", env ->
//                                        fileOverview(CreateQueryParam(env), "desc"))
//                                .dataFetcher("caseCountByDiagnosis", env ->
//                                        caseCountByDiagnosis(CreateQueryParam(env)))
//                                .dataFetcher("caseCountByGender", env ->
//                                        caseCountByGender(CreateQueryParam(env)))
//                                .dataFetcher("caseCountByBreed", env ->
//                                        caseCountByBreed(CreateQueryParam(env)))
//                                .dataFetcher("caseCountByNeuteredStatus", env ->
//                                        caseCountByNeuteredStatus(CreateQueryParam(env)))
//                                .dataFetcher("caseCountByStageOfDisease", env ->
//                                        caseCountByStageOfDisease(CreateQueryParam(env)))
//                                .dataFetcher("caseCountByDiseaseSite", env ->
//                                        caseCountByDiseaseSite(CreateQueryParam(env)))
//                        .dataFetcher("caseCountByStudyCode", env ->
//                                caseCountByStudyCode(CreateQueryParam(env)))
                )
                .build();
    }

//    private List<Map<String, Object>> caseCountByDiseaseSite(QueryParam params) throws IOException {
//
//        Map<String, Object> args = params.getArgs();
//        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
//        final Aggregation termQuery = Aggregation.of(agg ->
//                agg.terms(
//                        v -> v.field(ES_FIELDS.PRIMARY_DISEASE_SITE).size(ES_UNITS.DEFAULT_SIZE)
//                ));
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.DIAGNOSIS)
//                .size(0)
//                .aggregations(ES_PARAMS.TERMS_AGGS, termQuery)
//                .query(createBoolQuery(strIds, ES_FIELDS.DIAG_CASE_OF_CASE))
//        );
//        List<Map<String, Object>> result = esService.elasticSend(params.getReturnTypes(), request, esService.getAggregate());
//        return result;
//    }
//
//    private List<Map<String, Object>> caseCountByStageOfDisease(QueryParam param) throws IOException {
//        Map<String, Object> args = param.getArgs();
//        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
//        final Aggregation termQuery = Aggregation.of(agg ->
//                agg.terms(
//                        v -> v.field(ES_FIELDS.STAGE_OF_DISEASE).size(ES_UNITS.DEFAULT_SIZE)
//                ));
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.DIAGNOSIS)
//                .size(0)
//                .aggregations(ES_PARAMS.TERMS_AGGS, termQuery)
//                .query(createBoolQuery(strIds, ES_FIELDS.DIAG_CASE_OF_CASE))
//        );
//        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, esService.getAggregate());
//        return result;
//    }
//
//
//    private List<Map<String, Object>> caseCountByNeuteredStatus(QueryParam param) throws IOException {
//        Map<String, Object> args = param.getArgs();
//        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
//        final Aggregation termQuery = Aggregation.of(agg ->
//                agg.terms(
//                        v -> v.field(ES_FIELDS.NEUTERED_INDICATOR).size(ES_UNITS.DEFAULT_SIZE)
//                ));
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.DEMOGRAPHIC)
//                .size(0)
//                .aggregations(ES_PARAMS.TERMS_AGGS, termQuery)
//                .query(createBoolQuery(strIds, ES_FIELDS.DEMOG_OF_CASE_CASE))
//        );
//        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, esService.getAggregate());
//        return result;
//    }
//
//
//    private Query createBoolQuery(List<String> ids, String field) {
//
//        if (ids.size() == 0 || ids == null) return null;
//        List<Query> queries = new ArrayList<>();
//        ids.forEach((caseId)->{
//            queries.add(new Query.Builder().term(v->v.field(field).value(value->value.stringValue(caseId))).build());
//        });
//        Query query = new Query.Builder()
//                .bool(
//                        new BoolQuery
//                                .Builder()
//                                .should(queries)
//                                .build()
//                ).build();
//        return queries.size() == 0 ? null : query;
//    }
//
//    private List<Map<String, Object>> caseCountByBreed(QueryParam param) throws IOException {
//
//        Map<String, Object> args = param.getArgs();
//        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
//        final Aggregation termQuery = Aggregation.of(agg ->
//                agg.terms(
//                        v -> v.field(ES_FIELDS.BREED).size(ES_UNITS.DEFAULT_SIZE)
//                ));
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.DEMOGRAPHIC)
//                .size(0)
//                .aggregations(ES_PARAMS.TERMS_AGGS, termQuery)
//                .query(createBoolQuery(strIds, ES_FIELDS.DEMOG_OF_CASE_CASE))
//        );
//        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, esService.getAggregate());
//        return result;
//    }
//
//    // case ids exists -> show all types of aggregation
//    // others -> aggregation filter by case_ids
//    private List<Map<String, Object>> caseCountByGender(QueryParam param) throws IOException {
//        Map<String, Object> args = param.getArgs();
//        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
//        final Aggregation termQuery = Aggregation.of(agg ->
//                agg.terms(
//                        v -> v.field(ES_FIELDS.SEX).size(ES_UNITS.DEFAULT_SIZE)
//                ));
//        Query query = createBoolQuery(strIds, ES_FIELDS.DEMOG_OF_CASE_CASE);
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.DEMOGRAPHIC)
//                .size(0)
//                .aggregations(ES_PARAMS.TERMS_AGGS, termQuery)
//                .query(query)
//        );
//
//        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, esService.getAggregate());
//        return result;
//    }
//
//    private QueryParam CreateQueryParam(DataFetchingEnvironment env) {
//        return QueryParam.builder()
//                .args(env.getArguments())
//                .outputType(env.getFieldType())
//                .build();
//    }
//
//
//    // TODO Create Param Parse as a Class, caseIDS, size
//    // TODO ES Service Editing
//    private List<Map<String, Object>> caseOverview(QueryParam param, String sortDirection) throws IOException {
//        Query query = new Query.Builder()
//                .matchAll(v->v.queryName("CASE_OVERVIEW"))
//                .build();
//
//        Map<String, Object> args = param.getArgs();
//
//        int pageSize = (int) args.get(ES_PARAMS.PAGE_SIZE);
//        int offset = (int) args.get(ES_PARAMS.OFFSET);
//        String sortField = args.get(ES_PARAMS.ORDER_BY).equals("") ? ES_FIELDS.CASE_ID : (String) args.get(ES_PARAMS.ORDER_BY);
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.CASES)
//                .sort(s ->
//                        s.field(f ->
//                                f.field(sortField).order(getSortType(sortDirection))))
//                .size(pageSize)
//                .from(offset)
//                .query(query));
//        return esService.elasticSend(param.getReturnTypes(), request, esService.getDefault());
//    }
//
//    private List<Map<String, Object>> sampleOverview(QueryParam param, String sortDirection) throws IOException {
//        Query query = new Query.Builder()
//                .matchAll(v->v.queryName("SAMPLE_OVERVIEW"))
//                .build();
//
//        Map<String, Object> args = param.getArgs();
//        int pageSize = (int) args.get(ES_PARAMS.PAGE_SIZE);
//        int offset = (int) args.get(ES_PARAMS.OFFSET);
//        String sortField = args.get(ES_PARAMS.ORDER_BY).equals("") ? ES_FIELDS.SAMPLE_ID : (String) args.get(ES_PARAMS.ORDER_BY);
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.SAMPLES)
//                .sort(s ->
//                        s.field(f ->
//                                f.field(sortField).order(getSortType(sortDirection))))
//                .size(pageSize)
//                .from(offset)
//                .query(query));
//
//        return esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());
//    }
//
//    private List<Map<String, Object>> caseCountByDiagnosis(QueryParam param) throws IOException {
//
//        Map<String, Object> args = param.getArgs();
//        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
//        Query query = createBoolQuery(strIds, ES_FIELDS.DIAG_OF_CASE_CASE);
//        final Aggregation termQuery = Aggregation.of(agg ->
//                agg.terms(
//                        v -> v.field(ES_FIELDS.DISEASE_TERM).size(ES_UNITS.DEFAULT_SIZE)
//                ));
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.DIAGNOSIS)
//                .size(0)
//                .aggregations(ES_PARAMS.TERMS_AGGS, termQuery)
//                .query(query));
//
//        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request,esService.getAggregate());
//        return result;
//    }
//
//    private List<Map<String, Object>> fileOverview(QueryParam param, String sortDirection) throws IOException {
//        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
//        Query query = new Query.Builder()
//                .matchAll(v->v.queryName("FILE_OVERVIEW"))
//                .build();
//        Map<String, Object> args = param.getArgs();
//        // TODO
//        int pageSize = (int) args.get(ES_PARAMS.PAGE_SIZE);
//        int offset = (int) args.get(ES_PARAMS.OFFSET);
//        String sortField = args.get(ES_PARAMS.ORDER_BY).equals("") ? "file_name" : (String) args.get(ES_PARAMS.ORDER_BY);
//        SearchRequest request = SearchRequest.of(r->r
//                .index(ES_INDEX.FILES)
//                .sort(s ->
//                        s.field(f ->
//                                f.field(sortField).order(getSortType(sortDirection))))
//                .size(10)
//                .from(offset)
//                .query(query));
//        return esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());
//    }

}
