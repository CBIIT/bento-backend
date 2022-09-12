package gov.nih.nci.bento.model.search.query;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QueryResult<T> {
    private final T searchHits;
    private final int totalHits;

    @Builder
    public QueryResult(T searchHits, long totalHits) {
        this.searchHits = searchHits;
        this.totalHits = (int) totalHits;
    }
}
