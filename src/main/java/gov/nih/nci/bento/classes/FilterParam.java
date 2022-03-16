package gov.nih.nci.bento.classes;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class FilterParam {

    private final Map<String, Object> args;
    private final String selectedField;
    private final String subAggSelectedField;
    private boolean isExcludeFilter;
    private final QueryParam queryParam;
    private String defaultSortField;
    private TableParam tableParam;

    @Builder
    @SuppressWarnings("unchecked")
    public FilterParam(Map<String, Object> args, String selectedField, String subAggSelectedField, boolean isExcludeFilter, QueryParam queryParam, String defaultSortField) {
        this.args = args;
        this.selectedField = selectedField;
        this.subAggSelectedField = subAggSelectedField;
        this.isExcludeFilter = isExcludeFilter;
        this.queryParam = queryParam;
        this.defaultSortField = defaultSortField;
        if (queryParam != null) this.tableParam = queryParam.getTableParam();
    }
}
