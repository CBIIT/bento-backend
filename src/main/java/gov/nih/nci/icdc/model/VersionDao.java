package gov.nih.nci.icdc.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.nih.nci.icdc.service.Neo4JGraphQLService;

@Configuration
@PropertySource("classpath:application.properties")
public class VersionDao {

	@Autowired
	private Neo4JGraphQLService neo4jService;

	@Value("${graphql_api.version}")
	private String GraphQLAPIVersion;

	@Value("${rest_api.version}")
	private String RESTAPIVersion;

	@Value("${front_end.version}")
	private String FrontEndVersion;

	@Value("${api.version}")
	private String apiVersion;

	@Value("${neo4j_query.getversion}")
	private String neo4jQueryVersion;


	public String getGraphQLAPIVersion() {
		return GraphQLAPIVersion;
	}

	public void setGraphQLAPIVersion(String graphQLAPIVersion) {
		GraphQLAPIVersion = graphQLAPIVersion;
	}

	public String getRESTAPIVersion() {
		return RESTAPIVersion;
	}

	public void setRESTAPIVersion(String rESTAPIVersion) {
		RESTAPIVersion = rESTAPIVersion;
	}

	public String getFrontEndVersion() {
		return FrontEndVersion;
	}

	public void setFrontEndVersion(String frontEndVersion) {
		FrontEndVersion = frontEndVersion;
	}

	public String getDataModelVersion() throws UnirestException  {
		String jsonOutput = neo4jService.query(this.neo4jQueryVersion);
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(jsonOutput);
		JsonObject jsonObject = jsonTree.getAsJsonObject();
		JsonElement dataJson = jsonObject.get("data");
		JsonObject dataObject = dataJson.getAsJsonObject();
		
		return dataObject.get("numberOfStudies").toString();
	}




	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

}
