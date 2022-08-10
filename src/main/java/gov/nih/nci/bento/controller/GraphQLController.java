package gov.nih.nci.bento.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.error.BentoGraphQLException;
import gov.nih.nci.bento.error.BentoGraphqlError;
import gov.nih.nci.bento.graphql.BentoGraphQL;
import gov.nih.nci.bento.model.ConfigurationDAO;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class GraphQLController {

	private static final Logger logger = LogManager.getLogger(GraphQLController.class);

	private final ConfigurationDAO config;
	private final Gson gson;
	private final BentoGraphQL bentoGraphQL;

	public GraphQLController(ConfigurationDAO config, BentoGraphQL bentoGraphQL){
		this.config = config;
		this.bentoGraphQL = bentoGraphQL;
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	@CrossOrigin
	@RequestMapping(value = "/version", method = {RequestMethod.GET},
			produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	public ResponseEntity<String> getVersion(HttpEntity<String> httpEntity, HttpServletResponse response){
		logger.info("Hit end point:/version");
		String versionString = config.getBentoApiVersion();
		logger.info(versionString);
		return ResponseEntity.ok(gson.toJson(Map.of("version", versionString)));
	}

	@CrossOrigin
	@RequestMapping(value = {"/v1/graphql/", "/v1/public-graphql/"}, method = {RequestMethod.GET, RequestMethod.HEAD,
			RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.TRACE, RequestMethod.OPTIONS, RequestMethod.PATCH},
			produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	public ResponseEntity<String> getPrivateGraphQLResponseByGET(HttpEntity<String> httpEntity,
			HttpServletResponse response) {
		HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
		String error = ApiError.jsonApiError(new ApiError(status, "API will only accept POST requests"));
		return logAndReturnError(status, error);
	}

	@CrossOrigin
	@RequestMapping(value = "/v1/graphql/", method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	@ResponseBody
	public ResponseEntity<String> getPrivateGraphQLResponse(HttpEntity<String> httpEntity,
			HttpServletResponse response){
        logger.info("hit end point:/v1/graphql/");
        return getGraphQLResponse(httpEntity, response, bentoGraphQL.getPrivateGraphQL());
	}

	@CrossOrigin
	@RequestMapping(value = "/v1/public-graphql/", method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE + "; charset=utf-8")
	@ResponseBody
	public ResponseEntity<String> getPublicGraphQLResponse(HttpEntity<String> httpEntity, HttpServletResponse response){
        logger.info("hit end point:/v1/public-graphql/");
		return getGraphQLResponse(httpEntity, response, bentoGraphQL.getPublicGraphQL());
	}

	@ResponseBody
	private ResponseEntity<String> getGraphQLResponse(HttpEntity<String> httpEntity, HttpServletResponse response,
			GraphQL graphQL) {
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
			return logAndReturnError(HttpStatus.BAD_REQUEST, e.getMessage());
		}

		if ((operation.equals("query") && config.isAllowGraphQLQuery())
				|| (operation.equals("mutation") && config.isAllowGraphQLMutation())) {
			return ResponseEntity.ok(query(query, variables, graphQL));
		}
		else if(operation.equals("query") || operation.equals("mutation")){
			HttpStatus status = HttpStatus.FORBIDDEN;
			String error = ApiError.jsonApiError(status, "Request type has been disabled",
					operation+" operations have been disabled in the application configuration.");
			return logAndReturnError(status, error);
		}
		else {
			HttpStatus status = HttpStatus.BAD_REQUEST;
			String error = ApiError.jsonApiError(status, "Unknown operation in request",
					operation+" operation type is not recognized.");
			return logAndReturnError(status, error);
		}
	}

	private String query(String sdl, Map<String, Object> variables, GraphQL graphQL) {
		ExecutionInput.Builder builder = ExecutionInput.newExecutionInput().query(sdl);
		if (variables != null) {
			builder = builder.variables(variables);
		}
		ExecutionInput input = builder.build();
		ExecutionResult executionResult = graphQL.execute(input);
		Map<String, Object> standardResult = executionResult.toSpecification();
		return gson.toJson(standardResult);
	}

	private ResponseEntity logAndReturnError(HttpStatus status, BentoGraphQLException ex){
		BentoGraphqlError bentoGraphqlError = ex.getBentoGraphqlError();
		List<String> errors = bentoGraphqlError.getErrors();
		for(String error: errors){
			logger.error(error);
		}
		return ResponseEntity.status(status).body(gson.toJson(bentoGraphqlError));
	}

	private ResponseEntity logAndReturnError(HttpStatus status, List<String> errors){
		return logAndReturnError(status, new BentoGraphQLException(errors));
	}

	private ResponseEntity logAndReturnError(HttpStatus status, String error){
		ArrayList<String> errors = new ArrayList<>();
		errors.add(error);
		return logAndReturnError(status, errors);
	}
}
