package gov.nih.nci.bento.icdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.controller.GraphQLController;
import gov.nih.nci.bento.test.YamlTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GraphQLController.class)
public class QueryICDCVerificationTests {

    private static final Logger logger = LogManager.getLogger(QueryICDCVerificationTests.class);
    private static final String elasticSearchEndpoint = "/test/graphql/";
    private static final String testYamlFileLocation = "yaml/icdc_test_es_query.yml";
    private  static final Gson gson = new Gson();
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void elasticQueryAPI_Test() throws Exception {
        Yaml yaml = new Yaml(new Constructor(YamlTest.class));
        YamlTest singleTypeQuery = yaml.load(new ClassPathResource(testYamlFileLocation).getInputStream());
        for (YamlTest.YamlTestQuery q : singleTypeQuery.getQuery()) {
            logger.info("Testing: " + q.getName());
            MvcResult result  = this.mockMvc.perform(MockMvcRequestBuilders
                            .post(elasticSearchEndpoint)
                            .content(new ObjectMapper().writeValueAsString(q.getRequest()))
                            .characterEncoding("UTF-8"))
                    .andExpect(status().isOk()).andReturn();

            JsonObject jsonObject = gson.fromJson(result.getResponse().getContentAsString(), JsonObject.class);

            Set<String> resultKeySet;
            if (jsonObject.get("data").getAsJsonObject().get(q.getName()) instanceof JsonArray) {
                JsonArray jsonArray = (JsonArray) jsonObject.get("data").getAsJsonObject().get(q.getName());
                resultKeySet = jsonArray.get(0).getAsJsonObject().keySet();
            } else {
                JsonObject jsonArray = (JsonObject) jsonObject.get("data").getAsJsonObject().get(q.getName());
                resultKeySet = jsonArray.keySet();
            }
            Set<String> expectedSet = q.getExpectedKeySet();
            resultKeySet.forEach(key->{
                expectedSet.remove(key);
            });

            assertThat(resultKeySet.size(), greaterThan(0));
            assertThat(expectedSet.size(), equalTo(0));
        }
    }
}