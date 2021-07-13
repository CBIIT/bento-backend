package gov.nih.nci.bento.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class IndexController {

	private static final Logger logger = LogManager.getLogger(IndexController.class);

	
	
	@RequestMapping(value = "/", produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request) {
		
        return new ModelAndView("/index");
    }

	@CrossOrigin
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	@ResponseBody
	public String ping(HttpServletRequest request, HttpServletResponse response) {
		return "pong";
	}
}
