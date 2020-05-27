package gov.nih.nci.bento.service;

import gov.nih.nci.bento.error.ApiError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.nih.nci.bento.model.ConfigurationDAO;

@Service
public class Neo4JGraphQLService {

	
	private static final Logger logger = LogManager.getLogger(Neo4JGraphQLService.class);
	
	@Autowired
	private ConfigurationDAO config;

	public String query(String graphQLQuery) throws ApiError {
		logger.info("Query neo4j:  "+graphQLQuery);

		HttpResponse<JsonNode> jsonResponse;
		try {
			jsonResponse = Unirest.post(config.getNeo4jGraphQLEndPoint
					()).header("Content-Type", "application/json")
					.header("Authorization", config.getNeo4jHttpHeaderAuthorization()).header("accept", "application/json")
					.body(graphQLQuery).asJson();
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