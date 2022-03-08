package gov.nih.nci.bento.classes;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class QueryResult {
    private final List<Map<String, Object>> searchHits;
    private final int totalHits;

    @SuppressWarnings("unchecked")
    @Builder
    public QueryResult(List<Map<String, Object>> searchHits, long totalHits) {
        this.searchHits = searchHits;
        this.totalHits = (int) totalHits;
    }
}
