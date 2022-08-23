package gov.nih.nci.bento.search.result;

import org.opensearch.action.search.SearchResponse;

public interface TypeMapper<T> {
    T get(SearchResponse response);
}
