package gov.nih.nci.bento.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.error.ApiErrorWrapper;
import io.swagger.annotations.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.service.Neo4JGraphQLService;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;

@RestController
public class GraphQLController {

	private static final Logger logger = LogManager.getLogger(GraphQLController.class);

	@Autowired
	private ConfigurationDAO config;
	@Autowired
	private Neo4JGraphQLService neo4jService;
	
	public static final Gson GSON = new Gson();

	@CrossOrigin
	@RequestMapping(value = "/v1/graphql/", method = RequestMethod.GET)
	public String getGraphQLResponseByGET(HttpEntity<String> httpEntity, HttpServletResponse response){
		return ApiError.JsonApiError(new ApiError(HttpStatus.BAD_REQUEST, "API does not accept GET requests"));
	}

	@CrossOrigin
	@RequestMapping(value = "/v1/graphql/", method = RequestMethod.POST)
	@ResponseBody
	public String getGraphQLResponse(HttpEntity<String> httpEntity, HttpServletResponse response){

		logger.info("hit end point:/v1/graphql/");

		// Get graphql query from request
		String reqBody = httpEntity.getBody().toString();
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(reqBody, JsonObject.class);
		String operation;
		try{
			String sdl = new String(jsonObject.get("query").getAsString().getBytes(), "UTF-8");
			Parser parser = new Parser();
			Document document = parser.parseDocument(sdl);
			OperationDefinition def = (OperationDefinition) document.getDefinitions().get(0);
			operation = def.getOperation().toString().toLowerCase();
		}
		catch(Exception e){
			return ApiError.JsonApiError(HttpStatus.BAD_REQUEST, "No query in request", e.getMessage());
		}

		if ((operation.equals("query") && config.isAllowGraphQLQuery())
				|| (operation.equals("mutation") && config.isAllowGraphQLMutation())) {
			try{
				String responseText = "";
				responseText = neo4jService.query(reqBody);
				return responseText;
			}
			catch(ApiError e){
				return ApiError.JsonApiError(e);
			}
		} else {
			return ApiError.JsonApiError(HttpStatus.BAD_REQUEST, "Request type has been disabled", operation+"s have been disabled in the application configuration.");
		}

	}


}
