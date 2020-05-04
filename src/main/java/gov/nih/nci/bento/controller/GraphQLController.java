package gov.nih.nci.bento.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
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
	public void getGraphQLResponseByGET(HttpEntity<String> httpEntity, HttpServletResponse response)
			throws IOException, UnirestException {

		throw new UnirestException("Could not find the GET method for URL /Bento/v1/graphql/");
	}

	@CrossOrigin
	@RequestMapping(value = "/v1/graphql/", method = RequestMethod.POST)
	@ResponseBody
	public String getGraphQLResponse(HttpEntity<String> httpEntity, HttpServletResponse response)
			throws IOException, UnirestException, HttpRequestMethodNotSupportedException {

		logger.info("hit end point:/v1/graphql/");

		// Get graphql query from request
		String reqBody = httpEntity.getBody().toString();
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(reqBody, JsonObject.class);
		String sdl = new String(jsonObject.get("query").getAsString().getBytes(), "UTF-8");

		Parser parser = new Parser();
		Document document = parser.parseDocument(sdl);
		OperationDefinition def = (OperationDefinition) document.getDefinitions().get(0);

		if ((def.getOperation().toString().toLowerCase().equals("query") && config.isAllowGraphQLQuery())
				|| (def.getOperation().toString().toLowerCase().equals("mutation")
						&& config.isAllowGraphQLMutation())) {

			String responseText = "";
			if (("").equals(sdl)) {
				throw new HttpRequestMethodNotSupportedException("Invalid Graphql query");
			} else {

				responseText = neo4jService.query(reqBody);

				return responseText;
			}
		} else {
			throw new UnirestException("Invalid Graphql query");
		}

	}


}
