package gov.nih.nci.bento.search.result;

import org.elasticsearch.action.search.SearchResponse;

import java.util.Set;

public interface TypeMapper<T> {
    T get(SearchResponse response, Set<String> resultType);
}
