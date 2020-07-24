package gov.nih.nci.bento.controller;

import javax.servlet.http.HttpServletResponse;

import gov.nih.nci.bento.error.ApiError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
	@RequestMapping
			(value = "/v1/graphql/", method = {
				RequestMethod.GET, RequestMethod.HEAD, RequestMethod.PUT, RequestMethod.DELETE,
				RequestMethod.TRACE, RequestMethod.OPTIONS, RequestMethod.PATCH}
			)
	public ResponseEntity<String> getGraphQLResponseByGET(HttpEntity<String> httpEntity, HttpServletResponse response){
		HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
		String error = ApiError.JsonApiError(new ApiError(status, "API will only accept POST requests"));
		return logAndReturnError(status, error);
	}

	@CrossOrigin
	@RequestMapping(value = "/v1/graphql/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> getGraphQLResponse(HttpEntity<String> httpEntity, HttpServletResponse response){

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
			HttpStatus status = HttpStatus.BAD_REQUEST;
			String error = ApiError.JsonApiError(status, "Invalid query in request", e.getMessage());
			return logAndReturnError(status, error);
		}

		if ((operation.equals("query") && config.isAllowGraphQLQuery())
				|| (operation.equals("mutation"))) {
			try{
				String responseText = "";
				responseText = neo4jService.query(reqBody);
				return ResponseEntity.ok(responseText);
			}
			catch(ApiError e){
				String error = ApiError.JsonApiError(e);
				return logAndReturnError(e.getStatus(), error);
			}
		}
		else if(operation.equals("query") || operation.equals("mutation")){
			HttpStatus status = HttpStatus.FORBIDDEN;
			String error = ApiError.JsonApiError(status, "Request type has been disabled", operation+" operations have been disabled in the application configuration.");
			return logAndReturnError(status, error);
		}
		else {
			HttpStatus status = HttpStatus.BAD_REQUEST;
			String error = ApiError.JsonApiError(status, "Unknown operation in request", operation+" operation is not recognized.");
			return logAndReturnError(status, error);
		}

	}

	private ResponseEntity logAndReturnError(HttpStatus status, String error){
		logger.error(error);
		return ResponseEntity.status(status).body(error);
	}
}
