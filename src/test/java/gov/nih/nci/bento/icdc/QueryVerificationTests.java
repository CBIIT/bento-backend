package gov.nih.nci.bento.icdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nih.nci.bento.constants.Const;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GraphQLController.class)
public class QueryVerificationTests {

    private static final Logger logger = LogManager.getLogger(QueryVerificationTests.class);
    private static final String elasticSearchEndpoint = "/test/graphql/";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void elasticQueryAPI_Test() throws Exception {
        Yaml yaml = new Yaml(new Constructor(YamlTest.class));
        YamlTest singleTypeQuery = yaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_ICDC.TEST).getInputStream());
        for (YamlTest.YamlTestQuery q : singleTypeQuery.getQuery()) {
            logger.info("Testing: " + q.getName());
            this.mockMvc.perform(MockMvcRequestBuilders
                            .post(elasticSearchEndpoint)
                            .content(new ObjectMapper().writeValueAsString(q.getRequest()))
                            .characterEncoding("UTF-8"))
                    .andExpect(status().isOk());
        }
    }
}