package gov.nih.nci.bento;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.controller.GraphQLController;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@WebMvcTest(GraphQLController.class)
public class GraphQLControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfigurationDAO configurationDAO;

    /**
     * Confirm that the "/version" endpoint accepts GET requests and verify the following within the response:
     *     - Http Status Code is 200 (OK)
     *     - Content Type is "application/json;charset=utf-8"
     *     - Content matches the String "Bento API Version: xx.xx.xx" where the version number matches
     *       "bento.api.version" from the application.properties file
     *
     * @throws Exception
     */
    @Test
    public void versionEndpointTestGET() throws Exception {
        String expectedVersion = "Bento API Version: "+configurationDAO.getBentoApiVersion();
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/version"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=utf-8"))
                .andExpect(MockMvcResultMatchers.content().string(expectedVersion))
                .andReturn();
        //assert method to satisfy codacy requirement, this statement will not be reached if the test fails
        assertNotNull(result);
    }

    /**
     * Confirm that the "/version" endpoint does NOT accept POST requests and verify the following within the response:
     *     - Http Status Code is 405 (METHOD NOT ALLOWED)
     *
     * @throws Exception
     */
    @Test
    public void versionEndpointTestPOST() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.post("/version"))
                .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed())
                .andReturn();
        //assert method to satisfy codacy requirement, this statement will not be reached if the test fails
        assertNotNull(result);
    }

    /**
     * Confirm that the "/v1/graphql/" endpoint does NOT accept GET requests and verify the following within the
     * response:
     *     - Http Status Code is 405 (METHOD NOT ALLOWED)
     *
     * @throws Exception
     */
    @Test
    public void graphQLEndpointTestGET() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/v1/graphql/"))
                .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed())
                .andReturn();
        //assert method to satisfy codacy requirement, this statement will not be reached if the test fails
        assertNotNull(result);
    }

    /**
     * Confirm that the "/v1/graphql/" endpoint accepts POST requests and verify the following within the response when
     * sent an empty GraphQL request:
     *     - Http Status Code is 400 (BAD REQUEST)
     *
     * @throws Exception
     */
    @Test
    public void graphQLEndpointTestPOSTEmptyRequest() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/v1/graphql/")
                        .contentType("application/json")
                        .content("{\"query\":\"\",\"variables\":{}}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
        //assert method to satisfy codacy requirement, this statement will not be reached if the test fails
        assertNotNull(result);
    }

    /**
     * Confirm that the "/v1/graphql/" endpoint accepts POST requests and verify the following within the response when
     * sent a valid GraphQL request containing a query NOT in the schema:
     *     - Http Status Code is 200 (OK)
     *     - Content Type is "application/json;charset=utf-8"
     *     - Content contains the expected "ValidationError"
     *
     * @throws Exception
     */
    @Test
    public void graphQLEndpointTestPOSTTestQuery() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/v1/graphql/")
                        .contentType("application/json")
                        .content("{\"query\":\"{testQuery}\",\"variables\":{}}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=utf-8"))
                .andReturn();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(result.getResponse().getContentAsString(), JsonObject.class);
        assertTrue(jsonObject.keySet().contains("errors"));
        JsonObject error = jsonObject.getAsJsonArray("errors").get(0).getAsJsonObject();
        assertTrue(error.keySet().contains("extensions"));
        JsonObject extensions = error.getAsJsonObject("extensions");
        assertTrue(extensions.keySet().contains("classification"));
        assertEquals("ValidationError", extensions.get("classification").getAsString());
    }
}
