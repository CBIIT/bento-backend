package gov.nih.nci.icdc.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mashape.unirest.http.exceptions.UnirestException;

import gov.nih.nci.icdc.model.ConfigurationDAO;
import gov.nih.nci.icdc.service.Neo4JGraphQLService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "ICDC REST APIs")
@Component
@RestController
public class RESTController {

	private static final Logger logger = LogManager.getLogger(RESTController.class);

	@Autowired
	private Neo4JGraphQLService neo4jService;

	@Autowired
	private ConfigurationDAO config;
	
	@CrossOrigin
	@RequestMapping(value = "/api/ping", method = RequestMethod.GET)
	@ResponseBody
	public String ping(HttpServletRequest request, HttpServletResponse response) {
		logger.info("hit end point:/ping");
		return "pong";
	}
	
	

	@RequestMapping(value = "/authorize/accept", method = RequestMethod.GET)
	public void authorizeCallBack(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		String redirect_url = "http://localhost/?passed";
		if (null != cookies) {
			for (int i = 0; i < cookies.length; i++) {
				if ("access_token".equals(cookies[i].getName())) {
					System.out.println(cookies[i].getValue());
					HttpSession session = request.getSession();
					session.setAttribute("token", cookies[i].getValue());
				}
			
				response.addCookie(cookies[i]);
			}
		} else {
			redirect_url = config.getErrorRedirectURL();
		}

		response.setHeader("Location", redirect_url);
		response.setStatus(302);
	}


	
	@RequestMapping(value = "/test_token", method = RequestMethod.GET)
	public String testToken(HttpServletRequest request, HttpServletResponse response) {
		
		return "Pass Token Test";
	}

}
