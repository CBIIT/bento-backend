package gov.nih.nci.bento.model;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.constants.Const.ES_INDEX;
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

    // parameters used in queries
    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";

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
//                        .dataFetcher("caseCountByStudyCode", env ->
//                                        caseCountByStudyCode(CreateQueryParam(env)))


//                        .dataFetcher("caseCountByDiagnosis", env ->
//                                caseCountByDiagnosis(CreateQueryParam(env)))
                )
                .build();
    }

    private QueryParam CreateQueryParam(DataFetchingEnvironment env) {
        return QueryParam.builder()
                .args(env.getArguments())
                .outputType(env.getFieldType())
                .build();
    }

//    private List<Map<String, Object>>  caseCountByStudyCode(QueryParam param) {
//
//        Query query = new Query.Builder()
//                .matchAll(v->v.queryName("caseCountByStudyCode"))
//                .build();
//
//        Aggregate aggregate = new Aggregate.Builder()
//                .
//
//
//        Map<String, Object> args = param.getArgs();
//        List<String> ids = (List<String>) args.get("case_ids");
//
//
//        // if size(ids) ==0 -> show all aggregation
//        // if size(ids) > 0 -> aggregation filter by case_ids
//
//
////            caseCountByStudyCode(case_ids: [String] = []): [GroupCount] @cypher(statement: """
////    MATCH (s:study)<-[:member_of]-(c:case)
////      WHERE (size($case_ids) = 0 OR c.case_id IN $case_ids)
////    RETURN {
////      group: s.clinical_study_designation,
////      count: count(DISTINCT(c))
////    }
////  """, passThrough: true)
//
//    }

    // TODO Create Param Parse as a Class, caseIDS, Szie
    // TODO ES Service Editing
    private List<Map<String, Object>> caseOverview(QueryParam param, String sortDirection) throws IOException {
        Query query = new Query.Builder()
                .matchAll(v->v.queryName("CASE_OVERVIEW"))
                .build();

        Map<String, Object> args = param.getArgs();

        int pageSize = (int) args.get(PAGE_SIZE);
        int offset = (int) args.get(OFFSET);
        String sortField = args.get(ORDER_BY).equals("") ? "case_id" : (String) args.get(ORDER_BY);
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
        int pageSize = (int) args.get(PAGE_SIZE);
        int offset = (int) args.get(OFFSET);
        String sortField = args.get(ORDER_BY).equals("") ? "sample_id" : (String) args.get(ORDER_BY);
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

    private List<Map<String, Object>> caseCountByDiagnosis(QueryParam param) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        Map<String, Object> args = param.getArgs();
        List<String> ids = (List<String>) args.get("case_ids");
        List<Query> queries = new ArrayList<>();
        ids.forEach(id->{
            queries.add(new Query.Builder()
                    .term(v->v
                            .field("case_ids_case_to_member_of_to_study")
                            .value(value->value.stringValue(id)))
                    .build());

        });

        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(queries).build();

        Query query = new Query.Builder()
                .bool(boolQuery).build();

        SearchRequest request = SearchRequest.of(r->r
                .index(Const.ES_INDEX.STUDIES)
                .size(10)
                .query(query));

        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());

        return result;
    }

    private List<Map<String, Object>> fileOverview(QueryParam param, String sortDirection) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        Query query = new Query.Builder()
                .matchAll(v->v.queryName("FILE_OVERVIEW"))
                .build();
        Map<String, Object> args = param.getArgs();
        int pageSize = (int) args.get(PAGE_SIZE);
        int offset = (int) args.get(OFFSET);
        String sortField = args.get(ORDER_BY).equals("") ? "file_name" : (String) args.get(ORDER_BY);
        SearchRequest request = SearchRequest.of(r->r
                .index(ES_INDEX.FILES)
                .sort(s ->
                        s.field(f ->
                                f.field(sortField).order(getSortType(sortDirection))))
                .size(pageSize)
                .from(offset)
                .query(query));

        return esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());
    }

}
