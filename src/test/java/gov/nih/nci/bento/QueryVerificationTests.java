package gov.nih.nci.bento;

import gov.nih.nci.bento.controller.GraphQLController;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@WebMvcTest(GraphQLController.class)
public class QueryVerificationTests {

    private static final Logger logger = LogManager.getLogger(QueryVerificationTests.class);
    private static final String graphQLEndpoint = "/v1/graphql/";
    private List<Map<String, String>> testQueries;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Before
    public void init(){
        String testQueriesFile = configurationDAO.getTestQueriesFile();
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(testQueriesFile)) {
            Map<String, List<Map<String, String>>> yamlMap = yaml.load(inputStream);
            testQueries = yamlMap.get("tests");
        }
        catch(IOException e) {
            logger.warn("Unable to find or read non-db queries list file: "+testQueriesFile);
            logger.debug(e);
        }
        catch(YAMLException e){
            logger.warn("Unable to parse YAML from non-db queries list from file: "+testQueriesFile);
            logger.debug(e);
        }
    }

    @Test
    public void runTestQueries() throws Exception {
        Assert.assertNotNull(testQueries);
        for(Map<String, String> query: testQueries){
            logger.info("Testing: " + query.get("name"));
            this.mockMvc.perform(MockMvcRequestBuilders
                    .post(graphQLEndpoint)
                    .content(query.get("request"))
                    .characterEncoding("UTF-8"))
                    .andExpect(MockMvcResultMatchers.content().json(query.get("response")));
        }
    }

}