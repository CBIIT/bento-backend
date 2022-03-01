package gov.nih.nci.bento.classes;

import gov.nih.nci.bento.model.ITypeMapper;
import lombok.Builder;
import lombok.Getter;
import org.elasticsearch.action.search.SearchRequest;
@Getter
public class MultipleRequests {

    private final String name;
    private final SearchRequest request;
    private ITypeMapper typeMapper;

    @Builder
    public MultipleRequests(SearchRequest request, ITypeMapper typeMapper, String name) {
        this.name = name;
        this.request = request;
        this.typeMapper = typeMapper;
    }
}