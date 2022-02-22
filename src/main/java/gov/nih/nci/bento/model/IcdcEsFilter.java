package gov.nih.nci.bento.model;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.constants.Const.ES_INDEX;
import gov.nih.nci.bento.service.ESService;
import static gov.nih.nci.bento.utility.ElasticUtility.getSortType;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
                )
                .build();
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
        return esService.elasticSend(param.getReturnTypes(), request);
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

        return esService.elasticSend(param.getReturnTypes(), request);
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

        return esService.elasticSend(param.getReturnTypes(), request);
    }

}
