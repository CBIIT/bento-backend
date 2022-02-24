package gov.nih.nci.bento;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.service.ESService;
import gov.nih.nci.bento.service.connector.AbstractClient;
import gov.nih.nci.bento.service.connector.DefaultClient;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static gov.nih.nci.bento.constants.Const.ES_FIELDS;
import static gov.nih.nci.bento.utility.ElasticUtility.getSortType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith( SpringRunner.class )
@SpringBootTest
public class EsServiceTest {

    @Autowired
    ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();
    private RestClient client;
    List<Query> queries = new ArrayList<>();

    @Autowired
    private ConfigurationDAO config;


    private Map<String, Object> query;

    @Before
    public void init() throws IOException {
        AbstractClient restClient = new DefaultClient(config);
        client = restClient.getRestConnector();

        Map<String, Object> params = new HashMap<>();
        params.put("subject_ids", new ArrayList<>());
        query = esService.buildFacetFilterQuery(params);

        // TODO Optimize Performance
        queries = Arrays.asList(
            new Query.Builder().term(v->v.field("case_id").value(value->value.stringValue("NCATS-COP01-CCB010015"))).build(),
            new Query.Builder().term(v->v.field("case_id").value(value->value.stringValue("GLIOMA01-i_64C0"))).build()
        );
    }

    @Test
    public void testbuildListQuery() {
        Map<String, Object> params = Map.of(
                "param1", List.of("value1", "value2")
        );
        Map<String, Object> builtQuery = esService.buildListQuery(params, Set.of());
        assertNotNull(builtQuery);
        var query = (Map<String, Object>)builtQuery.get("query");
        assertNotNull(query);
        var bool = (Map<String, Object>)query.get("bool");
        assertNotNull(bool);
        var filter = (List<Map<String, Object>>)bool.get("filter");
        assertNotNull(filter);
        assertEquals(1, filter.size());
        var param1 = ((Map<String, List<String>>)filter.get(0).get("terms")).get("param1");
        assertEquals(2, param1.size());
        assertEquals("value1", param1.get(0));
        assertEquals("value2", param1.get(1));
    }

    @Test
    public void send_Test() throws IOException {

        final String index = "/samples/_count";
        Request sampleCountRequest = new Request("GET", index);
        sampleCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(sampleCountRequest);
        int count = jsonObject.get("count").getAsInt();
        assertThat(count).isGreaterThan(0);
    }

    @Test
    public void elastic_Test() throws IOException {

        Query query = new Query.Builder()
                .matchAll(v->v.queryName("TEST")).build();

        SearchRequest request = SearchRequest.of(r->r
                .index(Const.ES_INDEX.CASES)
                .sort(s -> s.field(f -> f.field(ES_FIELDS.CASE_ID).order(SortOrder.Asc)))
                .size(10)
                .query(query));

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("case_ids", "case_ids");
        returnTypes.put("cohort", "cohort");
        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request,esService.getDefault());
        assertThat(result.size()).isGreaterThan(0);
    }

    // COUNT TEST
    @Test
    public void elastic_query_one_case_Test() throws IOException {

        Query caseIds = new Query.Builder().term(v->v.field(ES_FIELDS.CASE_MEMBER_OF_STUDY).value(value->value.stringValue("NCATS-COP01-CCB070020"))).build();
        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(caseIds).build();

        Query query = new Query.Builder()
                            .bool(boolQuery).build();

        SearchRequest request = SearchRequest.of(r->r
                .index(Const.ES_INDEX.STUDIES)
                .size(10)
                .query(query));


        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("clinical_study_designation", "clinical_study_designation");
        List<Map<String, Object>> result = (List<Map<String, Object>>) esService.elasticSend(returnTypes, request, esService.getDefault());
        assertThat(result.size()).isGreaterThan(0);
    }

    @Test
    public void elastic_aggregation_many_cases_Test() throws IOException {

        Query query = new Query.Builder()
            .bool(
                new BoolQuery.Builder()
                        .should(queries).build()
            ).build();

        final Aggregation termAgg = Aggregation.of(a -> a.terms(v -> v.field(Const.ES_FIELDS.CLINICAL_STUDY))                                );
        SearchRequest request = SearchRequest.of(r->r
                .index(Const.ES_INDEX.CASES)
                .size(0)
                .query(query)
                .aggregations(Const.ES_PARAMS.TERMS_AGGS,termAgg)
        );

        List<Map<String, Object>> result = esService.elasticSend(null, request, esService.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ES_FIELDS.COUNT)).isNotNull();
        assertThat(result.get(0).get(ES_FIELDS.GROUP)).isNotNull();
    }


    @Test
    public void elastic_aggregation_no_cases_Test() throws IOException {

        final Aggregation termAgg = Aggregation.of(a -> a.terms(v -> v.field(ES_FIELDS.CLINICAL_STUDY))                                );
        SearchRequest request = SearchRequest.of(r->r
                .index(Const.ES_INDEX.CASES)
                .size(0)
                .aggregations(Const.ES_PARAMS.TERMS_AGGS, termAgg)
        );

        List<Map<String, Object>> result = esService.elasticSend(null, request, esService.getAggregate());
        assertThat(result.size()).isGreaterThan(0);
        assertThat(result.get(0).get(ES_FIELDS.COUNT)).isNotNull();
        assertThat(result.get(0).get(ES_FIELDS.GROUP)).isNotNull();
    }

    @Test
    public void fileOverViewTest() throws IOException {


        Query query = new Query.Builder()
                .matchAll(v->v.queryName("TEST")).build();

        SearchRequest request = SearchRequest.of(r->r
                .index(Const.ES_INDEX.FILES)
                .sort(s -> s.field(f -> f.field(ES_FIELDS.FILE_NAME).order(SortOrder.Asc)))
                .size(10)
                .from(0)
                .query(query));

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("file_name", "file_name");
        returnTypes.put("file_type", "file_type");
        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request,esService.getDefault());
        assertThat(result.size()).isGreaterThan(0);
    }


    private List<Map<String, Object>> fileOverview(QueryParam param, String sortDirection) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        Query query = new Query.Builder()
                .matchAll(v->v.queryName("FILE_OVERVIEW"))
                .build();
        Map<String, Object> args = param.getArgs();
        int pageSize = (int) args.get(Const.ES_PARAMS.PAGE_SIZE);
        int offset = (int) args.get(Const.ES_PARAMS.OFFSET);
        String sortField = args.get(Const.ES_PARAMS.ORDER_BY).equals("") ? "file_name" : (String) args.get(Const.ES_PARAMS.ORDER_BY);
        SearchRequest request = SearchRequest.of(r->r
                .index(Const.ES_INDEX.FILES)
                .sort(s ->
                        s.field(f ->
                                f.field(sortField).order(getSortType(sortDirection))))
                .size(pageSize)
                .from(offset)
                .query(query));

        return esService.elasticSend(param.getReturnTypes(), request,esService.getDefault());
    }


}
