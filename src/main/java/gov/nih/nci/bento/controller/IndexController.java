package gov.nih.nci.bento.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.mashape.unirest.http.exceptions.UnirestException;

import gov.nih.nci.bento.model.VersionDao;
import io.swagger.annotations.ApiOperation;

@Controller
public class IndexController {

	private static final Logger logger = LogManager.getLogger(IndexController.class);

	
	@Autowired
	private VersionDao versions;
	
	@RequestMapping(value = "/", produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request) {
		
        return new ModelAndView("/index");
    }
	
	
	

	@ApiOperation(value = "Get software version")
	@CrossOrigin
	@RequestMapping(value = "/version", method = RequestMethod.GET)
	@ResponseBody
	public HashMap<String, String> version(HttpServletRequest request, HttpServletResponse response) throws UnirestException {
		
		 HashMap<String, String> map = new HashMap<>(); 
		 map.put("DataModelVersion", versions.getDataModelVersion()); 
		 map.put("FrondEndVersion", versions.getFrontEndVersion());
		 map.put("GraphQLAPIVersion", versions.getGraphQLAPIVersion());
		 map.put("RESTAPIVersion", versions.getRESTAPIVersion());
		 
		return map;
	}
}
