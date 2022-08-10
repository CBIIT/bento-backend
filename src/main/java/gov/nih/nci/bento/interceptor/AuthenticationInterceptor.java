package gov.nih.nci.bento.interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nih.nci.bento.error.BentoGraphqlError;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class AuthenticationInterceptor implements HandlerInterceptor {
    private static final Logger logger = LogManager.getLogger(AuthenticationInterceptor.class);
    private static final String[] PRIVATE_ENDPOINTS = {"/v1/graphql/"};

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @Autowired
    private ConfigurationDAO config;

    @Override
    public boolean preHandle(final HttpServletRequest request, HttpServletResponse response, final Object handler) throws IOException {
        //Verify that the request is not for the version endpoint and that request authentication is enabled
        if (config.isAuthEnabled() && Arrays.asList(PRIVATE_ENDPOINTS).contains(request.getServletPath())){
            HttpURLConnection con = null;
            HashMap<String, Object> errorInfo = new HashMap<>();
            try {
                //Extract the cookies from the request then verify that there is at least 1 cookie
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    //Create a request to the authentication endpoint
                    URL url = new URL(config.getAuthEndpoint()+"/authenticated");
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    //Add the cookies to the authentication request
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Cookie cookie : cookies) {
                        stringBuilder.append(cookie.getName() + "=" + cookie.getValue() + ";");
                    }
                    con.setRequestProperty("Cookie", stringBuilder.toString());
                    //Verify that the response code is OK (200)
                    int responseCode = con.getResponseCode();
                    if (responseCode == HttpStatus.OK.value()) {
                        //Parse the response as JSON
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(
                                        con.getInputStream(), "utf-8"
                                ));
                        StringBuilder responseBuilder = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            responseBuilder.append(responseLine.trim());
                        }
                        JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                        //Verify that the "status" property in the response is true
                        if (jsonResponse.getBoolean("status")) {
                            //Return true to continue executing the request
                            return true;
                        }
                    }
                }
                //If there are no cookies or the response code is not OK (200) then update the response
                logAndReturnError(
                        "You must be logged in to use this API",
                        HttpStatus.UNAUTHORIZED.value(),
                        null,
                        response
                );
            }
            catch (JSONException|UnsupportedEncodingException ex){
                logAndReturnError(
                        "An error occurred while parsing the response from the authentication service",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex,
                        response
                );
            }
            catch (IOException ex) {
                logAndReturnError(
                        "An error occurred while querying the authentication service",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex,
                        response
                );
            }
            catch (Exception ex){
                logAndReturnError(
                        "An error occurred while verifying that the user is authenticated",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex,
                        response
                );
            }
            finally {
                try{
                    if (con != null){
                        if (con.getInputStream() != null){
                            con.getInputStream().close();
                        }
                        con.disconnect();
                    }
                }
                catch (Exception ex){
                    logger.warn("An exception occurred when trying to close the HttpURLConnection");
                    logger.warn(ex);
                }
            }
            //return false to stop the request
            return false;
        }
        //Return true to continue executing the request
        return true;
    }

    private HttpServletResponse logAndReturnError(String message, int responseCode, Exception ex,
            HttpServletResponse response) throws IOException {
        logger.error(message);
        if (ex != null){
            logger.error(ex);
        }
        BentoGraphqlError bentoGraphqlError = new BentoGraphqlError(Arrays.asList(new String[]{message}));
        response.getWriter().write(gson.toJson(bentoGraphqlError));
        response.setStatus(responseCode);
        return response;
    }
}
