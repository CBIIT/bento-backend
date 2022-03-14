package gov.nih.nci.bento.model;

import org.elasticsearch.action.search.SearchResponse;

import java.util.Map;

public interface TypeMapper<T> {
    T get(SearchResponse response, Map<String, String> resultType);
}
