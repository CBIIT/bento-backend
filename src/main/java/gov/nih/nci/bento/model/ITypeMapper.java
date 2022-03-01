package gov.nih.nci.bento.model;

import org.elasticsearch.action.search.SearchResponse;

import java.util.List;
import java.util.Map;

public interface ITypeMapper<T> {
    T getResolver(SearchResponse response, Map<String, String> resultType);
}
