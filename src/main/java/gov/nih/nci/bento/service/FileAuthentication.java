package gov.nih.nci.bento.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FileAuthentication {

    private static final Logger logger = LogManager.getLogger(FileAuthentication.class);
    private final ConfigurationDAO config;
    private final Gson gson;
    private final String SUCCESS_STATUS = "success";

    public FileAuthentication(ConfigurationDAO config, Gson gson) {
        this.config = config;
        this.gson = gson;
    }

    public boolean getFileAuth(HttpEntity<String> httpEntity) {
        // if authenticated is false, file auth is not activated
        if (config.isAuthenticated() && httpEntity.getHeaders().containsKey("cookie")) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                String cookie = String.valueOf(httpEntity.getHeaders().get("cookie"));
                headers.add("Cookie", cookie.substring(1, cookie.length() - 1));

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> res = restTemplate.postForEntity(
                        config.getFileAuthUrl(), new HttpEntity<>("", headers) , String.class);

                JsonObject jsonObject = gson.fromJson(res.getBody(), JsonObject.class);
                String status = String.valueOf(jsonObject.get("status"));
                if (status.equalsIgnoreCase(SUCCESS_STATUS)) return true;
            }
            catch(Exception e) {
                logger.error(e.getMessage());
                logger.warn("Exception occurred while initializing Redis filter sets");;
            }
        }
        return false;
    }


}
