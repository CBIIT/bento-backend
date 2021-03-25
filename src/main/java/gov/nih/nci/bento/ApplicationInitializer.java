package gov.nih.nci.bento;

import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.service.Neo4JGraphQLService;
import gov.nih.nci.bento.service.RedisService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationInitializer implements InitializingBean {

    private static final Logger logger = LogManager.getLogger(ApplicationInitializer.class);

    @Autowired
    private ConfigurationDAO config;
    @Autowired
    private Neo4JGraphQLService neo4jService;
    @Autowired
    private RedisService redisService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(redisService.isInitialized() && config.getRedisFilterEnabled()){
            boolean groupsListsInitialized = initGroupListsInCache();
            redisService.setGroupListsInitialized(groupsListsInitialized);
        }
    }

    private boolean initGroupListsInCache(){
        logger.info("Initializing group list in Redis");
        Yaml yaml = new Yaml();
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(config.getRedisInitQueries())) {
            Map<String, List<String>> obj = yaml.load(inputStream);
            List<String> queries = obj.get("queries");
            for (String query : queries) {
                String response = neo4jService.query(String.format("{\"query\":\"{%s{group, subjects}}\"}", query));
                //Convert query response to JSON
                JSONObject jsonObject = new JSONObject(response);
                //Extract "data" attribute from response as a JSON object
                jsonObject = jsonObject.getJSONObject("data");
                //Extract and loop through array of query responses
                JSONArray jsonQueries = jsonObject.getJSONArray(query);
                for (int i = 0; i < jsonQueries.length(); i++) {
                    //Get current response
                    JSONObject groupList = jsonQueries.getJSONObject(i);
                    //Store group attribute
                    String group = groupList.get("group").toString();
                    //Store and format subjects attribute
                    String subjectsString = groupList.get("subjects").toString();
                    subjectsString = subjectsString.replaceAll("\"", "");
                    subjectsString = subjectsString.replace("[", "");
                    subjectsString = subjectsString.replace("]", "");
                    //Split subjects attribute into an array of subject_ids
                    String[] subjects = subjectsString.split(",");
                    //Cache subject_ids for the current group
                    logger.info("Caching " + group);
                    redisService.cacheGroup(group, subjects);
                }
            }
            return true;
        } catch (IOException | YAMLException e) {
            logger.error(e);
            logger.warn(String.format("Unable to read redis filter initializations queries from %s",config.getRedisInitQueries()));
            logger.warn("Redis filtering will not be enabled");
        } catch (ApiError apiError) {
            logger.error(apiError.getStatus());
            logger.error(apiError.getErrors());
            logger.error(apiError.getMessage());
            logger.warn("Error occurred while querying Neo4j to initialize Redis filter sets");
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.warn("Exception occurred while initializing Redis filter sets");
        }
        return false;
    }
}
