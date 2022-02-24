package gov.nih.nci.bento.model;

import org.elasticsearch.action.search.SearchResponse;

import java.util.List;
import java.util.Map;

public interface ReturnTypeMapper {
    List<Map<String, Object>> getResolver(SearchResponse response, Map<String, String> resultType);
}
