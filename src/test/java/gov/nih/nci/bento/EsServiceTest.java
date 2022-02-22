package gov.nih.nci.bento;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
                .sort(s -> s.field(f -> f.field("case_id").order(SortOrder.Asc)))
                .size(10)
                .query(query));

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put("case_ids", "case_ids");
        returnTypes.put("cohort", "cohort");
        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request);
        assertThat(result.size()).isGreaterThan(0);
    }
}
