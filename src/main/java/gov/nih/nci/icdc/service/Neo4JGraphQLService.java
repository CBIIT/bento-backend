package gov.nih.nci.icdc.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.nih.nci.icdc.error.ResourceNotFoundException;
import gov.nih.nci.icdc.model.ConfigurationDAO;

@Service
public class Neo4JGraphQLService {

	
	private static final Logger logger = LogManager.getLogger(Neo4JGraphQLService.class);
	
	@Autowired
	private ConfigurationDAO config;

	public String query(String graphQLQuery) throws UnirestException {
		logger.info("Query neo4j:  "+graphQLQuery);

		HttpResponse<JsonNode> jsonResponse;
		try {

			jsonResponse = Unirest.post(config.getNeo4jGraphQLEndPoint
					()).header("Content-Type", "application/json")
					.header("Authorization", config.getNeo4jHttpHeaderAuthorization()).header("accept", "application/json")
					.body(graphQLQuery).asJson();

		} catch (UnirestException e) {
			logger.error("Exception in function query() "+e.getStackTrace());
			throw new UnirestException(e);
		}

		JsonNode neo4jResponse = jsonResponse.getBody();
		// if neo4j response an error will throw that error to the front end
		if (neo4jResponse.getObject().has("errors")) {
			logger.error("Exception in function query() "+neo4jResponse.getObject().get("errors").toString());
			throw new ResourceNotFoundException(neo4jResponse.getObject().get("errors").toString());
		}
		return neo4jResponse.toString();

	}
	

}