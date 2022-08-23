package gov.nih.nci.bento.model.search.filter;

import lombok.Builder;
import lombok.Getter;
import org.elasticsearch.search.sort.SortOrder;

@Getter
public class TableParam {

    private final int pageSize;
    private final int offSet;
    private final SortOrder sortDirection;
    private final String orderBy;

    @Builder
    public TableParam(int pageSize, int offSet, SortOrder sortDirection, String orderBy) {
        this.pageSize = pageSize;
        this.offSet = offSet;
        this.sortDirection = sortDirection;
        this.orderBy = orderBy;
    }
}
