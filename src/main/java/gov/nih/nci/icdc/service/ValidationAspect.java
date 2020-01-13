package gov.nih.nci.icdc.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.login.AccountExpiredException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.jsonwebtoken.ExpiredJwtException;

@Component
@Aspect
public class ValidationAspect {

	private static final Logger logger = LogManager.getLogger(ValidationAspect.class);

//	@Pointcut("execution (* gov.nih.nci.icdc.controller.RESTController.getPrograms(..))"
//			+ "||execution (* gov.nih.nci.icdc.controller.RESTController.getProgramStudies(..))"
//			+ "||execution (* gov.nih.nci.icdc.controller.RESTController.getStudies(..))"
//			+ "||execution (* gov.nih.nci.icdc.controller.RESTController.getStudyCases(..))"
//			+ "||execution (* gov.nih.nci.icdc.controller.RESTController.getCases(..))"
//			+ "||execution (* gov.nih.nci.icdc.controller.RESTController.authorizeCallBack(..))"
//			)
//	public void allPublicMethods() {
//
//	}

	@Pointcut("execution (* gov.nih.nci.icdc.controller.RESTController.TestToken(..))"
			+ "||execution (* gov.nih.nci.icdc.controller.RESTController.authorizeCallBack(..))")
	public void advisedMethods() {
		// nothing here
	}

	/**
	 * Validate if the request is valid request. 1. Check request session timeout 2.
	 * Check token is valid or not 3. Check token is expired or not
	 *
	 * @param JoinPoint method will be executed next
	 * @return Execption will be threw if one of checks fails otherwise get into
	 *         JoinPoint
	 * @throws AccountExpiredException
	 * @throws HttpRequestMethodNotSupportedException
	 * @throws ParseException
	 * @throws JWTDecodeException
	 * @throws ExpiredJwtException
	 */

	@Before("advisedMethods()")
	public void validateBefore(JoinPoint joinPoint) throws IllegalArgumentException, AccountExpiredException,
			HttpRequestMethodNotSupportedException, ExpiredJwtException, JWTDecodeException, ParseException {

		logger.info("AOP : Validate Request");
			Object[] args = joinPoint.getArgs();
			HttpServletRequest request = null;
			for (Object arg : args) {
				if (arg instanceof HttpServletRequest) {
					request = (HttpServletRequest) arg;
				}
			}
			if (null != request) {
				logger.info("AOP : Validate request session");
				// check session

				if (isSessionExpired(request)) {
					logger.info("AOP : User's session expired");
					throw new IllegalArgumentException("User's session expired");
				}
				logger.info("AOP : Session is good");
				logger.info("AOP : Validate request token");
				// check token
				String token = isCookiesHasToken(request);
				if (token == null) {
					logger.info("AOP : user's token is null");
					throw new IllegalArgumentException("User's token is null");
				} else {
					isGoodCookies(token);
				}
			} else {
				throw new IllegalArgumentException("Bad Request");
			}
		

	}

	/**
	 * Check if the request has token in the cookie
	 *
	 * @param request HttpServletRequest request
	 * @return boolean true -> expired | false -> not expire
	 */
	public boolean isSessionExpired(HttpServletRequest request) {
		// return request.getRequestedSessionId() != null &&
		// request.isRequestedSessionIdValid();
		return false;
	}

	/**
	 * Check if the request has token in the cookie
	 *
	 * @param request HttpServletRequest request
	 * @return token token | ""
	 */
	public String isCookiesHasToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		String token = "";
		if (null != cookies) {
			for (int i = 0; i < cookies.length; i++) {
				if ("access_token".equals(cookies[i].getName())) {
					token = cookies[i].getValue();
					return token;
				}
			}
		}
		return null;
	}

	/**
	 * Validate Token and verify the expire date
	 *
	 * @param token encoded token from Fence
	 */
	public void isGoodCookies(String token) throws ExpiredJwtException, JWTDecodeException, ParseException {
		DecodedJWT jwt = JWT.decode(token);
		SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss a");
		Date today = sdf.parse((String) new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss a").format(new Date()));
		Date expire = jwt.getExpiresAt();
		if (expire.compareTo(today) <= 0) {
			logger.info("AOP : User's token is expired");
			logger.info("AOP : Current time is:" + today + "  . The expiration date is " + expire);
			logger.info("AOP : Token is " + token);
			throw new ExpiredJwtException(null, null,
					"The token is expired . " + " Current time is:" + today + "  . The expiration date is " + expire, null);
		}

	}

}
