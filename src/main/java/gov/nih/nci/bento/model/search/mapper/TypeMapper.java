package gov.nih.nci.bento.model.search.mapper;

import org.elasticsearch.action.search.SearchResponse;

public interface TypeMapper<T> {
    T get(SearchResponse response);
}
