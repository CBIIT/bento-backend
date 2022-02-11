package gov.nih.nci.bento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.service.ESService;
import gov.nih.nci.bento.service.connector.AbstractClient;
import gov.nih.nci.bento.service.connector.DefaultClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensearch.client.Request;
import org.opensearch.client.RestClient;
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
    private ESService esService;
    private Gson gson = new GsonBuilder().serializeNulls().create();
    private RestClient client;

    @Autowired
    private ConfigurationDAO config;

    @Before
    public void init() {
        AbstractClient restClient = new DefaultClient(config);
        client = restClient.getRestConnector();
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
        Map<String, Object> params = new HashMap<>();
        params.put("subject_ids", new ArrayList<>());
        Map<String, Object> query = esService.buildFacetFilterQuery(params);
        Request sampleCountRequest = new Request("GET", index);

        sampleCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(sampleCountRequest);
        int count = jsonObject.get("count").getAsInt();
        assertThat(count).isGreaterThan(0);
    }

    @Test
    public void multiple_requests_Test() throws IOException, InterruptedException {

        final String index = "/samples/_count";
        Map<String, Object> params = new HashMap<>();
        params.put("subject_ids", new ArrayList<>());
        Map<String, Object> query = esService.buildFacetFilterQuery(params);
        Request sampleCountRequest = new Request("GET", index);

        sampleCountRequest.setJsonEntity(gson.toJson(query));
        HashMap<String, Request> requestHashMap = new HashMap<>();
        requestHashMap.put("SAMPLE", sampleCountRequest);

        Map<String, JsonObject> resultMaps = esService.asyncSend(requestHashMap);
        assertThat(resultMaps.size()).isGreaterThan(0);
    }
}
