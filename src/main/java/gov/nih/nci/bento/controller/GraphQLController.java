package gov.nih.nci.bento.controller;

import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.GraphQLWrapper;
import gov.nih.nci.bento.service.Neo4JGraphQLService;
import gov.nih.nci.bento.service.RedisService;
import graphql.language.OperationDefinition.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GraphQLController {

	@Autowired
	private ConfigurationDAO config;
	@Autowired
	private Neo4JGraphQLService neo4jService;
	@Autowired
	private RedisService redisService;

	private static final Logger logger = LogManager.getLogger(GraphQLController.class);
	private static List<String> listOfServiceQueries = null;

	@PostConstruct
	public void initializeListOfServiceQueries(){
		//Populate list of YAML queries
		Yaml yaml = new Yaml();
		String yamlFile = config.getRedisFilterQueries();
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(yamlFile)) {
			Map<String, List<String>> obj = yaml.load(inputStream);
			listOfServiceQueries = obj.get("queries");
		}
		catch(IOException e) {
			logger.warn("Unable to find or read non-db queries list file: "+yamlFile);
			logger.debug(e);
		}
		catch(YAMLException e){
			logger.warn("Unable to parse YAML from non-db queries list from file: "+yamlFile);
			logger.debug(e);
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
	public ResponseEntity<String> getGraphQLResponse(HttpEntity<String> httpEntity, HttpServletResponse response) {
		logger.info("hit end point:/v1/graphql/");
		//Initialize the request object
		GraphQLWrapper graphQLWrapper = new GraphQLWrapper(httpEntity, listOfServiceQueries);
		//Get operation
		Operation operation = graphQLWrapper.getOperation();
		//Check if operation is allowed
		if ((operation == Operation.QUERY && config.isAllowGraphQLQuery()) ||
				(operation == Operation.MUTATION && config.isAllowGraphQLMutation())) {
			try {
				//Execute database request
				String databaseRequest = graphQLWrapper.getDatabaseRequest();
				String databaseResponse = null;
				try {
					if (databaseRequest != null) {
						if (config.getRedisEnabled()) {
							databaseResponse = redisService.getCachedValue(databaseRequest);
							if (databaseResponse == null) {
								databaseResponse = neo4jService.query(databaseRequest);
								redisService.cacheValue(databaseRequest, databaseResponse);
							}
						} else {
							databaseResponse = neo4jService.query(databaseRequest);
						}
					} else {
						logger.info("No database queries found in request");
					}
				} catch (ApiError apiError) {
					return logAndReturnError(apiError.getStatus(), apiError.getMessage(), apiError.getErrors());
				}
				//Execute service request
				String serviceResponse = null;
				//Check if the request contains service queries and filter lists have been initialized
				if (graphQLWrapper.hasServiceQueries() && redisService.isGroupListsInitialized()) {
					//Get maps of filter parameters for each service query
					HashMap<String, HashMap<String, String[]>> filterParamsMap = graphQLWrapper.getFilterParameters();
					//Initialize data JSON object to hold query results
					JSONObject data = new JSONObject();
					//Iterate through each service query in the filterParamsMap
					for (String queryName : filterParamsMap.keySet()) {
						//Get filter parameter map for the current query
						HashMap<String, String[]> filtersMap = filterParamsMap.get(queryName);
						//Create unions of filter parameter sets for each category
						ArrayList<String> unionKeys = new ArrayList<>();
						for (String category : filtersMap.keySet()) {
							String unionKey = category + "_union";
							String[] filters = filtersMap.get(category);
							for (int i = 0; i < filters.length; i++) {
								//replace spaces with underscore and format key as "category:filter"
								filters[i] = category + ":" + filters[i].replace(" ", "_");
							}
							redisService.unionStore(unionKey, filters);
							unionKeys.add(unionKey);
						}
						//Get intersection of all of the unions from above
						String[] intersection;
						if (unionKeys.size() == 0) {
							intersection = redisService.getCachedSet("all").toArray(new String[0]);
						} else {
							intersection = redisService.getIntersection(unionKeys.toArray(new String[0])).toArray(new String[0]);
						}
						//Store query results in a JSON object
						JSONObject filterQuery = new JSONObject();
						filterQuery.put("subjects", intersection);
						filterQuery.put("numberOfSubjects", intersection.length);
						//Add the query results JSON object to the output JSON object
						data.put(queryName, filterQuery);
					}
					//Format all filter query outputs and convert to a string
					try {
						serviceResponse = new JSONObject().put("data", data).toString();
					} catch (Exception e) {
						logger.error("Could not get service response");
						logger.error(e.getMessage());
					}
				}
				if (serviceResponse != null && databaseResponse != null) {
					//Merge responses
					JSONObject dataService = null;
					JSONObject dataDatabase = null;
					try {
						JSONObject mergedData = new JSONObject();
						dataService = new JSONObject(serviceResponse);
						dataService = (JSONObject) dataService.get("data");
						for (String name : JSONObject.getNames(dataService)) {
							mergedData.put(name, dataService.get(name));
						}

						dataDatabase = new JSONObject(databaseResponse);
						dataDatabase = (JSONObject) dataDatabase.get("data");
						for (String name : JSONObject.getNames(dataDatabase)) {
							mergedData.put(name, dataDatabase.get(name));
						}
						return ResponseEntity.ok(new JSONObject().put("data", mergedData).toString());
					} catch (JSONException e) {
						HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
						String error = ApiError.jsonApiError(new ApiError(status, e.toString()));
						return logAndReturnError(status, error);
					}
				} else if (serviceResponse == null && databaseResponse == null) {
					HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
					String error = "No response was generated by the query\n" +
							"database query: " + databaseRequest;
					return logAndReturnError(status, error);

				} else if (serviceResponse == null) {
					return ResponseEntity.ok(databaseResponse);
				} else {
					return ResponseEntity.ok(serviceResponse);
				}
			} catch (Exception e) {
				HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
				String error = ApiError.jsonApiError(status, "Error occurred while executing request: " + e.getMessage(), e.toString());
				return logAndReturnError(status, error);
			}
		} else if (operation.equals("query") || operation.equals("mutation")) {
			HttpStatus status = HttpStatus.FORBIDDEN;
			String error = ApiError.jsonApiError(status, "Request type has been disabled", operation + " operations have been disabled in the application configuration.");
			return logAndReturnError(status, error);
		} else {
			HttpStatus status = HttpStatus.BAD_REQUEST;
			String error = ApiError.jsonApiError(status, "Unknown operation in request", operation + " operation is not recognized.");
			return logAndReturnError(status, error);
		}
	}

	private ResponseEntity logAndReturnError(HttpStatus status, String message) {
		logger.error(message);
		return ResponseEntity.status(status).body(message);
	}

	private ResponseEntity logAndReturnError(HttpStatus status, String message, List<String> errors) {
		for(String err : errors){
			message += "\n" + err;
		}
		message += "\n";
		return logAndReturnError(status, message);
	}

}
