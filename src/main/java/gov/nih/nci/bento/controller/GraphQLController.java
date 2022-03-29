package gov.nih.nci.bento.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.search.GraphQLBuilder;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@DependsOn({"neo4jDataFetcher"})
@RequiredArgsConstructor
public class GraphQLController {

	private static final Logger logger = LogManager.getLogger(GraphQLController.class);
	private final ConfigurationDAO config;
	private final GraphQLBuilder graphQL;
	private static final Gson gson = new Gson();

	@CrossOrigin
	@RequestMapping(value = "/version", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	public ResponseEntity<String> getVersion(HttpEntity<String> httpEntity, HttpServletResponse response){
		logger.info("Hit end point:/version");
		String versionString = "Bento API Version: "+config.getBentoApiVersion();
		logger.info(versionString);
		return ResponseEntity.ok(versionString);
	}

	@CrossOrigin
	@ApiIgnore
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
		String operation = new String();
		try {
			String body = httpEntity.getBody();
			JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
			operation = getOperation(jsonObject);

			Map<String, Object> variables = gson.fromJson(jsonObject.get("variables"), Map.class);
			if (isValid(operation)) return ResponseEntity.ok(graphQL.query(getQuery(jsonObject), variables));

		} catch(Exception e) {
			String error = ApiError.jsonApiError(HttpStatus.BAD_REQUEST, "Invalid query in request", e.getMessage());
			logger.error(error);
		}

		if (operation.equals("query") || operation.equals("mutation")) {
			String error = ApiError.jsonApiError(HttpStatus.FORBIDDEN, "Request type has been disabled", operation+" operations have been disabled in the application configuration.");
			return logAndReturnError(HttpStatus.FORBIDDEN, error);
		}
		String error = ApiError.jsonApiError(HttpStatus.BAD_REQUEST, "Unknown operation in request", operation+" operation is not recognized.");
		return logAndReturnError(HttpStatus.BAD_REQUEST, error);
	}

	@CrossOrigin
	@RequestMapping(value = "/test/graphql/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	@ResponseBody
	public ResponseEntity<String> getGraphQLTest(@RequestBody String jsonBody){
		logger.info("hit end point:/test/graphql/");
		// Get graphql query from request
		try {
			JsonObject jsonObject = gson.fromJson(jsonBody, JsonObject.class);
			Map<String, Object> variables = gson.fromJson(jsonObject.get("variables"), Map.class);
			return ResponseEntity.ok(graphQL.query(getQuery(jsonObject), variables));

		} catch(Exception e) {
			String error = ApiError.jsonApiError(HttpStatus.BAD_REQUEST, "Invalid query in request", e.getMessage());
			logger.error(error);
		}
		throw new RuntimeException();
	}

	private boolean isValid(String operation) {
		return (operation.equals("query") && config.isAllowGraphQLQuery())
				|| (operation.equals("mutation") && config.isAllowGraphQLMutation());
	}

	private String getOperation(JsonObject jsonObject) {
		Parser parser = new Parser();
		Document document = parser.parseDocument(getQuery(jsonObject));
		OperationDefinition def = (OperationDefinition) document.getDefinitions().get(0);
		return def.getOperation().toString().toLowerCase();
	}

	private String getQuery(JsonObject jsonObject) {
		return new String(jsonObject.get("query").getAsString().getBytes(), StandardCharsets.UTF_8);
	}

	private ResponseEntity logAndReturnError(HttpStatus status, String error){
		logger.error(error);
		return ResponseEntity.status(status).body(error);
	}

}
