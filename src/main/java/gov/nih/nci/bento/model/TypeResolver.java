package gov.nih.nci.bento.model;

import co.elastic.clients.elasticsearch.core.SearchResponse;

import java.util.List;
import java.util.Map;

public interface TypeResolver {
    List<Map<String, Object>> getResolver(SearchResponse<Object> response, Map<String, String> resultType);
}
