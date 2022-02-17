package gov.nih.nci.bento.service;


import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;

@Service("ESService_Alternate")
public class ESServiceAlternate {
    private static final Logger logger = LogManager.getLogger(ESServiceAlternate.class);


    private static final String SORT_KEY = "order_by";
    private static final String ES_ORDER_KEY = "order";
    private static final String GRAPHQL_ORDER_KEY = "sort_direction";
    private static final String[] SORTING_KEYS = {SORT_KEY, ES_ORDER_KEY, GRAPHQL_ORDER_KEY};
    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

    private final static Map<String, String> FORMATTING_VARIABLES_MAP = Map.ofEntries(
            Map.entry("first", "size"),
            Map.entry("offset", "from")
    );

    private final static Map<String, Object> VARIABLE_DEFAULTS = Map.ofEntries(
            Map.entry("size", 10000),
            Map.entry("from", 0)
    );

    @Autowired
    ConfigurationDAO config;

    private RestClient client;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    // Base on host name to use signed request (AWS) or not (local)
    public RestClient searchClient(String serviceName, String region) {
        String host = config.getEsHost().trim();
        String scheme = config.getEsScheme();
        int port = config.getEsPort();
        if (config.getEsSignRequests()) {
            AWS4Signer signer = new AWS4Signer();
            signer.setServiceName(serviceName);
            signer.setRegionName(region);
            HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
            return RestClient.builder(new HttpHost(host, port, scheme)).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)).build();
        } else {
            var lowLevelBuilder = RestClient.builder(new HttpHost(host, port, scheme));
            return lowLevelBuilder.build();
        }
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing Elasticsearch client");
        client = searchClient("es", "us-east-1");
    }

    @PreDestroy
    private void close() throws IOException {
        client.close();
    }

    public JsonObject send(Request request) throws IOException {
        Response response = client.performRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            String msg = "Elasticsearch returned code: " + statusCode;
            logger.error(msg);
            throw new IOException(msg);
        }
        return getJSonFromResponse(response);
    }

    private JsonObject getJSonFromResponse(Response response) throws IOException {
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        return jsonObject;
    }

}
