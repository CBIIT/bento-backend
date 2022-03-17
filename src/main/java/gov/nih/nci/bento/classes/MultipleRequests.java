package gov.nih.nci.bento.classes;

import gov.nih.nci.bento.search.result.TypeMapperTest;
import lombok.Builder;
import lombok.Getter;
import org.elasticsearch.action.search.SearchRequest;
@Getter
public class MultipleRequests {

    private final String name;
    private final SearchRequest request;
    private TypeMapperTest typeMapper;

    @Builder
    public MultipleRequests(SearchRequest request, TypeMapperTest typeMapper, String name) {
        this.name = name;
        this.request = request;
        this.typeMapper = typeMapper;
    }
}