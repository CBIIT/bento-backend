package gov.nih.nci.bento;

import gov.nih.nci.bento.controller.GraphQLController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(GraphQLController.class)
@TestPropertySource(properties = { "allow_graphql_query=false" })
public class AppPropertyTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Confirm that the "/v1/graphql/" endpoint accepts POST requests and verify the following within the response when
     * false allow_graphql_query request:
     *     - Http Status Code is 403 (FORBIDDEN REQUEST)
     *
     * @throws Exception
     */
    @Test
    public void graphQLEndpointTestPOSTTestQuery() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v1/graphql/")
                        .contentType("application/json")
                        .content("{\"query\":\"{testQuery}\",\"variables\":{}}"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=utf-8"))
                .andReturn();
    }
}
