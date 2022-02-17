package gov.nih.nci.bento.elasticsearch;

import com.google.gson.Gson;
import org.opensearch.client.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ESRequestBuilder {

    private static Gson gson = new Gson();

    public static Request buildFilterSearch(String endpoint, Map<String, Object> params, String defaultSortProperty) {
        Request request = new Request("GET", endpoint);
        request.setJsonEntity(buildSearchRequestBody(params, defaultSortProperty));
        return request;
    }

    public static Request buildFilterCount(String endpoint, Map<String, Object> params, List<String> countProperties) {
        Request request = new Request("GET", endpoint);
        request.setJsonEntity(buildCountRequestBody(params, countProperties));
        return request;
    }

    private static String buildSearchRequestBody(Map<String, Object> params, String defaultSortProperty) {
        ESRequestBody body = new ESRequestBody(params, defaultSortProperty);
        return gson.toJson(body);
    }

    private static String buildCountRequestBody(Map<String, Object> params, List<String> countProperties) {
        ESRequestBody body = new ESRequestBody(params, countProperties);
        return gson.toJson(body);
    }



}
