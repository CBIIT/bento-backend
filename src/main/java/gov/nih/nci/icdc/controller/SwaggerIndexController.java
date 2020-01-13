package gov.nih.nci.icdc.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SwaggerIndexController {
	
	private static final Logger logger = LogManager.getLogger(SwaggerIndexController.class);
	
	@RequestMapping("/api/doc/api")
	public String greeting() {
		logger.info("Hit the swgger api page");
		return "doc/api/index";
	}
	
}
