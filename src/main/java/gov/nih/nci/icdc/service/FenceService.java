package gov.nih.nci.icdc.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.nih.nci.icdc.model.ConfigurationDAO;
@Service
public class FenceService {

	private static final Logger logger = LogManager.getLogger(FenceService.class);
	
	@Autowired
	private ConfigurationDAO config;


	// exchange code for token
	
	public String getToken(String code) throws UnirestException {
		HttpResponse<JsonNode> jsonResponse;
		try {
			jsonResponse= Unirest.post(config.getFenceTokenExchange())
							 	   .basicAuth(config.getFenceId(), config.getFenceCredential())
							 	   .header("Content-Type", "application/x-www-form-urlencoded")
							 	   .field("grant_type", "authorization_code")
							 	   .field("code",code)
							 	   .field("redirect_uri",config.getFenceRedirect())
							 	   .field("client_id", config.getFenceId())
							 	   .asJson();

		} catch (UnirestException e) {
			logger.error("Exception in function getToken from fence "+e.getStackTrace());
			throw new UnirestException(e);
		}
		return jsonResponse.getBody().toString();
	
	}





	
	
}
