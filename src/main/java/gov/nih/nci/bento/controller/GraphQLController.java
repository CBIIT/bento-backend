package gov.nih.nci.bento.controller;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.service.Neo4jDataFetcher;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.ExecutionInput;
import graphql.schema.GraphQLSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.SchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import gov.nih.nci.bento.service.RedisService;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@RestController
public class GraphQLController {

	private static final Logger logger = LogManager.getLogger(GraphQLController.class);

	@Autowired
	private ConfigurationDAO config;
	@Autowired
	private RedisService redisService;
	@Autowired
	private Neo4jDataFetcher dataFetcherInterceptor;

	private Gson gson = new Gson();
	private GraphQL graphql;

	@PostConstruct
	public void init() throws IOException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource("classpath:" + config.getSchemaFile());
		File schemaFile = resource.getFile();
		String schemaString = Files.readString(schemaFile.toPath());
		SchemaConfig schemaConfig = new SchemaConfig();

		GraphQLSchema schema = SchemaBuilder.buildSchema(schemaString, schemaConfig, dataFetcherInterceptor);
		graphql = GraphQL.newGraphQL(schema).build();
	}


	@CrossOrigin
	@RequestMapping(value = "/version", method = {RequestMethod.GET})
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
			)
	public ResponseEntity<String> getGraphQLResponseByGET(HttpEntity<String> httpEntity, HttpServletResponse response){
		HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
		String error = ApiError.jsonApiError(new ApiError(status, "API will only accept POST requests"));
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
		String query_key;
		String sdl;
		Map<String, Object> variables;
		try{
			sdl = new String(jsonObject.get("query").getAsString().getBytes(), "UTF-8");
			Parser parser = new Parser();
			Document document = parser.parseDocument(sdl);
			query_key = document.toString();
			JsonElement rawVar = jsonObject.get("variables");
			variables = gson.fromJson(rawVar, Map.class);
			if (null != rawVar) {
				query_key +=  "::" + rawVar.toString();
			}
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
			String responseText;
			if (config.getRedisEnabled()) {
				responseText = redisService.getQueryResult(query_key);
				if ( null == responseText) {
					responseText = query(sdl, variables);
					redisService.setQueryResult(query_key, responseText);
				}
			} else {
				responseText = query(sdl, variables);
			}
			return ResponseEntity.ok(responseText);
		}
		else if(operation.equals("query") || operation.equals("mutation")){
			HttpStatus status = HttpStatus.FORBIDDEN;
			String error = ApiError.jsonApiError(status, "Request type has been disabled", operation+" operations have been disabled in the application configuration.");
			return logAndReturnError(status, error);
		}
		else {
			HttpStatus status = HttpStatus.BAD_REQUEST;
			String error = ApiError.jsonApiError(status, "Unknown operation in request", operation+" operation is not recognized.");
			return logAndReturnError(status, error);
		}

	}

	private String query(String sdl, Map<String, Object> variables) {
		ExecutionInput.Builder builder = ExecutionInput.newExecutionInput().query(sdl);
		if (variables != null) {
			builder = builder.variables(variables);
		}
		ExecutionInput input = builder.build();
		ExecutionResult executionResult = graphql.execute(input);
		Map<String, Object> standardResult = executionResult.toSpecification();
		return gson.toJson(standardResult);
	}

	private ResponseEntity logAndReturnError(HttpStatus status, String error){
		logger.error(error);
		return ResponseEntity.status(status).body(error);
	}
}
