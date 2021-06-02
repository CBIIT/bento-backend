package gov.nih.nci.bento.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.service.Neo4jDataFetcher;
import gov.nih.nci.bento.service.RedisFilterDataFetcher;
import gov.nih.nci.bento.service.RedisService;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.SchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
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
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@DependsOn({"neo4jDataFetcher", "redisService"})
public class GraphQLController {

	private static final Logger logger = LogManager.getLogger(GraphQLController.class);

	@Autowired
	private ConfigurationDAO config;
	@Autowired
	private RedisService redisService;
	@Autowired
	private Neo4jDataFetcher dataFetcherInterceptor;
	@Autowired
	private RedisFilterDataFetcher redisFilterDataFetcher;


	private Gson gson = new GsonBuilder().serializeNulls().create();
	private GraphQL graphql;

	@PostConstruct
	public void init() throws IOException {
		initGraphQL();
		if (config.getRedisEnabled() && config.isRedisFilterEnabled()){
			boolean redisInitialized = initRedisFiltering();
			//Initialize redis filtering and check if initialization was successful
			if(!redisInitialized){
				//If initialization failed, set redis filtering to disabled in the config and reinitialize GraphQL to
				//remove the redis schema
				config.setRedisFilterEnabled(false);
				initGraphQL();
			}
		}
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
			return ResponseEntity.ok(query(query, variables));
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

	private void initGraphQL() throws IOException {
		GraphQLSchema neo4jSchema = getNeo4jSchema();
		GraphQLSchema redisSchema = getRedisSchema();
		GraphQLSchema newSchema = mergeSchema(neo4jSchema, redisSchema);
		graphql = GraphQL.newGraphQL(newSchema).build();
	}

	private boolean initRedisFiltering(){
		logger.info("Initializing group list in Redis");
		Yaml yaml = new Yaml();
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(config.getRedisFilterInitQueriesFile())) {
			Map<String, Object> yamlMap = yaml.load(inputStream);
			List<String> queries = (List<String>) yamlMap.get("queries");
			for (String query : queries) {
				String response = query(String.format("{%s{group subjects}}", query), new HashMap<>());
				//Convert query response to JSON
				JSONObject jsonObject = new JSONObject(response);
				//Extract "data" attribute from response as a JSON object
				jsonObject = jsonObject.getJSONObject("data");
				//Extract and loop through array of query responses
				JSONArray jsonQueries = jsonObject.getJSONArray(query);
				for (int i = 0; i < jsonQueries.length(); i++) {
					//Get current response
					JSONObject groupList = jsonQueries.getJSONObject(i);
					//Check for required response fields
					if (!groupList.has("group") || !groupList.has("subjects")){
						continue;
					}
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
			redisService.setParameterMappings((HashMap<String, String>) yamlMap.get("parameter_mappings"));
			return true;
		} catch (IOException | YAMLException e) {
			logger.error(e);
			logger.warn(String.format("Unable to read redis filter initializations queries from %s",config.getRedisFilterInitQueriesFile()));
			logger.warn("Redis filtering will not be enabled");
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.warn("Exception occurred while initializing Redis filter sets");
		}
		return false;
	}

	private GraphQLSchema getRedisSchema() throws IOException {
		if (config.getRedisEnabled() && config.isRedisFilterEnabled()){
			File schemaFile = new DefaultResourceLoader().getResource("classpath:" + config.getRedisSchemaFile()).getFile();
			return new SchemaGenerator().makeExecutableSchema(new SchemaParser().parse(schemaFile), redisFilterDataFetcher.buildRuntimeWiring());
		}
		else{
			return null;
		}
	}

	@NotNull
	private GraphQLSchema getNeo4jSchema() throws IOException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource("classpath:" + config.getSchemaFile());
		File schemaFile = resource.getFile();
		String schemaString = Files.readString(schemaFile.toPath());
		SchemaConfig schemaConfig = new SchemaConfig();

		GraphQLSchema neo4jSchema = SchemaBuilder.buildSchema(schemaString, schemaConfig, dataFetcherInterceptor);
		return neo4jSchema;
	}


	private GraphQLSchema mergeSchema(GraphQLSchema schema1, GraphQLSchema schema2) {
		String QUERY_TYPE_NAME = "Query";
		String MUTATION_TYPE_NAME = "Mutation";
		String SUBSCRIPTION_TYPE_NAME = "Subscription";

		if (schema1 == null) {
			return schema2;
		}
		if (schema2 == null) {
			return schema1;
		}

		var builder = GraphQLSchema.newSchema(schema1);
		var codeRegistry2 = schema2.getCodeRegistry();
		builder.codeRegistry(schema1.getCodeRegistry().transform( crBuilder -> {crBuilder.dataFetchers(codeRegistry2); crBuilder.typeResolvers(codeRegistry2);}));
		var allTypes = new HashMap<String, GraphQLNamedType>(schema1.getTypeMap());
		allTypes.putAll(schema2.getTypeMap());

		//Remove individual schema query, mutation, and subscription types from all types to prevent naming conflicts
		allTypes = removeQueryMutationSubscription(allTypes, schema1);
		allTypes = removeQueryMutationSubscription(allTypes, schema2);

		//Add merged query, mutation, and subscription types
		GraphQLNamedType mergedQuery = mergeType(schema1.getQueryType(), schema2.getQueryType());
		if (mergedQuery != null){
			allTypes.put(QUERY_TYPE_NAME, mergedQuery);
		}

		GraphQLNamedType mergedMutation = mergeType(schema1.getMutationType(), schema2.getMutationType());
		if (mergedMutation != null){
			allTypes.put(MUTATION_TYPE_NAME, mergedMutation);
		}

		GraphQLNamedType mergedSubscription = mergeType(schema1.getSubscriptionType(), schema2.getSubscriptionType());
		if (mergedSubscription != null){
			allTypes.put(SUBSCRIPTION_TYPE_NAME, mergedSubscription);
		}

		builder.query((GraphQLObjectType) allTypes.get(QUERY_TYPE_NAME));
		builder.mutation((GraphQLObjectType) allTypes.get(MUTATION_TYPE_NAME));
		builder.subscription((GraphQLObjectType) allTypes.get(SUBSCRIPTION_TYPE_NAME));

		builder.clearAdditionalTypes();
		allTypes.values().forEach(builder::additionalType);

		return builder.build();
	}

	private HashMap<String, GraphQLNamedType> removeQueryMutationSubscription(
			HashMap<String, GraphQLNamedType> allTypes, GraphQLSchema schema){
		try{
			String name = schema.getQueryType().getName();
			allTypes.remove(name);
		}
		catch (NullPointerException e){}

		try{
			String name = schema.getMutationType().getName();
			allTypes.remove(name);
		}
		catch (NullPointerException e){}

		try{
			String name = schema.getSubscriptionType().getName();
			allTypes.remove(name);
		}
		catch (NullPointerException e){}

		return allTypes;
	}

	private GraphQLNamedType mergeType(GraphQLObjectType type1, GraphQLObjectType type2) {
		if (type1 == null) {
			return type2;
		}
		if (type2 == null) {
			return type1;
		}
		var builder = GraphQLObjectType.newObject(type1);
		type2.getFieldDefinitions().forEach(builder::field);
		return builder.build();
	}
}
