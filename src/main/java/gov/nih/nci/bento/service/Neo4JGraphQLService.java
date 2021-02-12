package gov.nih.nci.bento.service;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.service.Neo4jRequest;
import gov.nih.nci.bento.service.TranslationProvider;
import gov.nih.nci.bento.service.Neo4jService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphql.Cypher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
public class Neo4JGraphQLService {

	
	private static final Logger logger = LogManager.getLogger(Neo4JGraphQLService.class);

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_JSON = "application/json";
	private static final String AUTHORIZATION = "Authorization";
	private static final String ACCEPT = "accept";

	private Gson gson = new Gson();

	@Autowired
	private ConfigurationDAO config;

	@Autowired
	private TranslationProvider translationProvider;

	@Autowired
	private Neo4jService neo4jService;

//	@PostConstruct
	public void loadSchema(){
		// Set socket timeout value here, to only set it once
		// connection timeout set to 10s, and socket timeout set to 300s
		Unirest.setTimeouts(10000, 300000);;
		logger.info("Loading schema into Neo4j");
		HttpResponse<JsonNode> jsonResponse;
		try {
			ResourceLoader resourceLoader = new DefaultResourceLoader();
			Resource resource = resourceLoader.getResource("classpath:"+config.getSchemaFile());
			Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
			String schema = FileCopyUtils.copyToString(reader);
			String endpoint = config.getNeo4jGraphQLEndPoint()+config.getNeo4jGraphQLSchemaEndpoint();
			jsonResponse = Unirest
					.post(endpoint)
					.header(CONTENT_TYPE, APPLICATION_JSON)
					.header(AUTHORIZATION, config.getNeo4jHttpHeaderAuthorization())
					.header(ACCEPT, APPLICATION_JSON)
					.body(schema)
					.asJson();
			if(jsonResponse.getStatus() == 404){
				logger.error("Schema load failure, unable to connect to endpoint: "+endpoint);
			}
			else if(jsonResponse.getStatus() == 500){
				logger.error("Schema load failure, Neo4j was unable to parse the schema file.");
			}
			else if(jsonResponse.getStatus() == 200){
				logger.info("Schema loaded successfully");
			}
			else{
				//Handles any response that is not one of the expected possible responses
				logger.error("Schema load failure, Bento received an unexpected response from from the" +
						" Neo4j GraphQL endpoint: "+jsonResponse.getStatus()+"-"+jsonResponse.getStatusText());
			}
		} catch (UnirestException e1) {
			logger.error("Unable to load the GraphQL schema into Neo4j");
			logger.error(e1.toString());
		} catch (IOException e2) {
			logger.error("Unable to read the GraphQL schema");
			logger.error(e2.toString());
		}
	}

	public String query(String graphQLQuery) throws ApiError {
		logger.info("Query neo4j:  "+graphQLQuery);
		Cypher cypher = translationProvider.translateToCypher(graphQLQuery);
		//Store the cypher in a serializable format
		Neo4jRequest request = new Neo4jRequest(cypher);
		logger.info("Cypher: "+request.getQuery());

//		neo4jService.printGreeting("Ming");

		HttpResponse<JsonNode> jsonResponse;
		try {
			jsonResponse = Unirest.post(config.getNeo4jGraphQLEndPoint())
					.header("Content-Type", "application/json")
					.header("Authorization", config.getNeo4jHttpHeaderAuthorization()).header("accept", "application/json")
					.body(gson.toJson(request)).asJson();
			if (jsonResponse.getStatus() != 200) {
				throw new ApiError(HttpStatus.resolve(jsonResponse.getStatus()), "Exception occurred while querying database service", jsonResponse.getStatusText());
			}
		} catch (UnirestException e) {
			logger.error("Exception in function query() "+e.toString());
			throw new ApiError(HttpStatus.SERVICE_UNAVAILABLE, "Exception occurred while querying database service", e.getMessage());
		}

		JsonNode neo4jResponse = jsonResponse.getBody();
		if (neo4jResponse.getObject().has("errors")) {
			String errors = neo4jResponse.getObject().get("errors").toString();
			logger.error("Exception in function query() "+errors);
			throw new ApiError(HttpStatus.BAD_REQUEST, "Request resulted in response containing errors", errors);
		}
		return neo4jResponse.toString();
	}
	

}