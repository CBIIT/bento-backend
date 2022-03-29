package gov.nih.nci.bento.classes;

import gov.nih.nci.bento.search.result.TypeMapper;
import lombok.Builder;
import lombok.Getter;
import org.elasticsearch.action.search.SearchRequest;
@Getter
public class MultipleRequests {

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