package gov.nih.nci.bento.model.search;

import gov.nih.nci.bento.model.search.mapper.TypeMapper;
import lombok.Builder;
import lombok.Getter;
import org.opensearch.action.search.SearchRequest;

@Getter
public class MultipleRequests<T> {

    private final String name;
    private final SearchRequest request;
    private final TypeMapper typeMapper;

    @Builder
    public MultipleRequests(SearchRequest request, TypeMapper typeMapper, String name) {
        this.name = name;
        this.request = request;
        this.typeMapper = typeMapper;
    }
}