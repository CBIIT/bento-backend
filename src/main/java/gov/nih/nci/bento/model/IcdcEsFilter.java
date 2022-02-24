package gov.nih.nci.bento.model;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.constants.Const.ES_FIELDS;
import gov.nih.nci.bento.constants.Const.ES_INDEX;
import gov.nih.nci.bento.constants.Const.ES_PARAMS;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gov.nih.nci.bento.utility.ElasticUtility.getSortType;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class IcdcEsFilter implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(IcdcEsFilter.class);

    @Autowired
    ESService esService;

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
//                        .dataFetcher("caseCountByStudyCode", env ->
//                                caseCountByStudyCode(CreateQueryParam(env)))
                )
                .build();
    }

    private Query getCaseIdsQuery(List<String> caseStrIds, String field) {

        if (caseStrIds.size() == 0 || caseStrIds == null) return null;
        List<Query> queries = new ArrayList<>();
        caseStrIds.forEach((caseId)->{
            if (!caseId.equals("")) {
                queries.add(new Query.Builder().term(v->v.field(field).value(value->value.stringValue(caseId))).build());
            }
        });
        Query query = new Query.Builder()
                    .bool(
                            new BoolQuery
                                .Builder()
                                .should(queries)
                                .build()
                    ).build();
        return queries.size() == 0 ? null : query;
    }

    // case ids exists -> show all types of aggregation
    // others -> aggregation filter by case_ids
    private Object caseCountByGender(QueryParam param) throws IOException {
        Map<String, Object> args = param.getArgs();
        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        final Aggregation termsQuery = Aggregation.of(a -> a.terms(v -> v.field(ES_FIELDS.SEX)));
        Query query = getCaseIdsQuery(strIds, ES_FIELDS.CASE_ID);
        SearchRequest request = SearchRequest.of(r->r
                .index(ES_INDEX.DEMOGRAPHIC)
                .size(0)
                .aggregations(ES_PARAMS.TERMS_AGGS, termsQuery)
                .query(query)
        );
        return esService.elasticSend(param.getReturnTypes(), request, esService.getAggregate());
    }

    private QueryParam CreateQueryParam(DataFetchingEnvironment env) {
        return QueryParam.builder()
                .args(env.getArguments())
                .outputType(env.getFieldType())
                .build();
    }


    // TODO Create Param Parse as a Class, caseIDS, Szie
    // TODO ES Service Editing
    private List<Map<String, Object>> caseOverview(QueryParam param, String sortDirection) throws IOException {
        Query query = new Query.Builder()
                .matchAll(v->v.queryName("CASE_OVERVIEW"))
                .build();

        Map<String, Object> args = param.getArgs();

        int pageSize = (int) args.get(ES_PARAMS.PAGE_SIZE);
        int offset = (int) args.get(ES_PARAMS.OFFSET);
        String sortField = args.get(ES_PARAMS.ORDER_BY).equals("") ? ES_FIELDS.CASE_ID : (String) args.get(ES_PARAMS.ORDER_BY);
        SearchRequest request = SearchRequest.of(r->r
                .index(ES_INDEX.CASES)
                .sort(s ->
                        s.field(f ->
                                f.field(sortField).order(getSortType(sortDirection))))
                .size(pageSize)
                .from(offset)
                .query(query));
        return esService.elasticSend(param.getReturnTypes(), request, esService.getDefault());
    }

    private List<Map<String, Object>> sampleOverview(QueryParam param, String sortDirection) throws IOException {
        Query query = new Query.Builder()
                .matchAll(v->v.queryName("SAMPLE_OVERVIEW"))
                .build();

        Map<String, Object> args = param.getArgs();
        int pageSize = (int) args.get(ES_PARAMS.PAGE_SIZE);
        int offset = (int) args.get(ES_PARAMS.OFFSET);
        String sortField = args.get(ES_PARAMS.ORDER_BY).equals("") ? ES_FIELDS.SAMPLE_ID : (String) args.get(ES_PARAMS.ORDER_BY);
        SearchRequest request = SearchRequest.of(r->r
                .index(ES_INDEX.SAMPLES)
                .sort(s ->
                        s.field(f ->
                                f.field(sortField).order(getSortType(sortDirection))))
                .size(pageSize)
                .from(offset)
                .query(query));

        return esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());
    }

    private List<Map<String, Object>>  caseCountByStudyCode(QueryParam param) throws IOException {

        Query query = null;
        Map<String, Object> args = param.getArgs();
        List<String> caseStrIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        if (caseStrIds.size() > 0) {
            List<Query> queries = null;
            caseStrIds.forEach((caseId)->{
                queries.add(new Query.Builder().term(v->v.field("case_id").value(value->value.stringValue(caseId))).build());
            });
            query = new Query.Builder()
                    .bool(
                            new BoolQuery.Builder()
                                    .should(queries).build()
                    ).build();
        }
        // if size(ids) ==0 -> show all aggregation
        // if size(ids) > 0 -> aggregation filter by case_ids
        final Aggregation termsQuery = Aggregation.of(a -> a.terms(v -> v.field(ES_FIELDS.CLINICAL_STUDY)));
        Query finalQuery = query;
        SearchRequest request = SearchRequest.of(r->r
                .index(ES_INDEX.CASES)
                .size(0)
                .aggregations(ES_PARAMS.TERMS_AGGS, termsQuery)
                .query(finalQuery)
        );
        return esService.elasticSend(param.getReturnTypes(), request, esService.getAggregate());
    }

    private List<Map<String, Object>> caseCountByDiagnosis(QueryParam param) throws IOException {

        Map<String, Object> args = param.getArgs();
        List<String> strIds = (List<String>) args.get(ES_PARAMS.CASE_IDS);
        Query query = getCaseIdsQuery(strIds, ES_FIELDS.DIAG_OF_CASE_CASE);
        final Aggregation termsQuery = Aggregation.of(a -> a.terms(v -> v.field(ES_FIELDS.DISEASE_TERM)));
        SearchRequest request = SearchRequest.of(r->r
                .index(ES_INDEX.DIAGNOSIS)
                .size(0)
                .aggregations(ES_PARAMS.TERMS_AGGS, termsQuery)
                .query(query));

        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request,esService.getAggregate());
        return result;
    }

    private List<Map<String, Object>> fileOverview(QueryParam param, String sortDirection) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        Query query = new Query.Builder()
                .matchAll(v->v.queryName("FILE_OVERVIEW"))
                .build();
        Map<String, Object> args = param.getArgs();
        // TODO
        int pageSize = (int) args.get(ES_PARAMS.PAGE_SIZE);
        int offset = (int) args.get(ES_PARAMS.OFFSET);
        String sortField = args.get(ES_PARAMS.ORDER_BY).equals("") ? "file_name" : (String) args.get(ES_PARAMS.ORDER_BY);
        SearchRequest request = SearchRequest.of(r->r
                .index(ES_INDEX.FILES)
                .sort(s ->
                        s.field(f ->
                                f.field(sortField).order(getSortType(sortDirection))))
                .size(10)
                .from(offset)
                .query(query));
        return esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());
    }

}
