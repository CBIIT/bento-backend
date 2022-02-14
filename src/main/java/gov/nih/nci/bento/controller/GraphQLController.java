package gov.nih.nci.bento.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.GraphQLBuilder;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@DependsOn({"neo4jDataFetcher"})
@RequiredArgsConstructor
public class GraphQLController {

	private static final Logger logger = LogManager.getLogger(GraphQLController.class);
	private final ConfigurationDAO config;
	private final GraphQLBuilder graphQL;

	@CrossOrigin
	@RequestMapping(value = "/version", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	public ResponseEntity<String> getVersion(HttpEntity<String> httpEntity, HttpServletResponse response){
		logger.info("Hit end point:/version");
		String versionString = "Bento API Version: "+config.getBentoApiVersion();
		logger.info(versionString);
		return ResponseEntity.ok(versionString);
	}

	@CrossOrigin
	@RequestMapping
			(value = "/v1/graphql/", method = {
				RequestMethod.GET, RequestMethod.HEAD, RequestMethod.PUT, RequestMethod.DELETE,
				RequestMethod.TRACE, RequestMethod.OPTIONS, RequestMethod.PATCH}
				, produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	public ResponseEntity<String> getGraphQLResponseByGET(HttpEntity<String> httpEntity, HttpServletResponse response){
		HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
		String error = ApiError.jsonApiError(new ApiError(status, "API will only accept POST requests"));
		return logAndReturnError(status, error);
	}

	@CrossOrigin
	@RequestMapping(value = "/v1/graphql/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	@ResponseBody
	public ResponseEntity<String> getGraphQLResponse(HttpEntity<String> httpEntity, HttpServletResponse response){

		logger.info("hit end point:/v1/graphql/");

		// Get graphql query from request
		String reqBody = httpEntity.getBody().toString();
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(reqBody, JsonObject.class);
		String query;
		Map<String, Object> variables;
		String operation;
		try{
			query = new String(jsonObject.get("query").getAsString().getBytes(), "UTF-8");
			JsonElement rawVar = jsonObject.get("variables");
			variables = gson.fromJson(rawVar, Map.class);
			Parser parser = new Parser();
			Document document = parser.parseDocument(query);
			OperationDefinition def = (OperationDefinition) document.getDefinitions().get(0);
			operation = def.getOperation().toString().toLowerCase();
		}
		catch(Exception e){
			HttpStatus status = HttpStatus.BAD_REQUEST;
			String error = ApiError.jsonApiError(status, "Invalid query in request", e.getMessage());
			return logAndReturnError(status, error);
		}

		if ((operation.equals("query") && config.isAllowGraphQLQuery())
				|| (operation.equals("mutation") && config.isAllowGraphQLMutation())) {
			return ResponseEntity.ok(graphQL.query(query, variables));
		}
		else if(operation.equals("query") || operation.equals("mutation")){
			HttpStatus status = HttpStatus.FORBIDDEN;
			String error = ApiError.jsonApiError(status, "Request type has been disabled", operation+" operations have been disabled in the application configuration.");
			return logAndReturnError(status, error);
		}
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String error = ApiError.jsonApiError(status, "Unknown operation in request", operation+" operation is not recognized.");
		return logAndReturnError(status, error);
	}

	private ResponseEntity logAndReturnError(HttpStatus status, String error){
		logger.error(error);
		return ResponseEntity.status(status).body(error);
	}

}
