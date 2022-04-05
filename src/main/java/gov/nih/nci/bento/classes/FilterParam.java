package gov.nih.nci.bento.classes;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class FilterParam {

    private final Map<String, Object> args;
    private final String selectedField;
    private final String subAggSelectedField;
    private final boolean isExcludeFilter;
    private final QueryParam queryParam;
    private final String defaultSortField;
    private final String customOrderBy;
    private final boolean isRangeFilter;
    private final boolean isNestedFilter;
    private TableParam tableParam;
    private final String nestedPath;
    private final Set<String> nestedFields;

    @Builder
    public FilterParam(Map<String, Object> args, String selectedField, String subAggSelectedField,
                       boolean isExcludeFilter, QueryParam queryParam, String defaultSortField, String customOrderBy,
                       boolean isRangeFilter, String nestedPath, boolean isNestedFilter, Set<String> nestedFields) {
        this.args = args;
        this.selectedField = selectedField;
        this.subAggSelectedField = subAggSelectedField;
        this.isExcludeFilter = isExcludeFilter;
        this.queryParam = queryParam;
        this.defaultSortField = defaultSortField;
        this.customOrderBy = customOrderBy;
        this.isRangeFilter = isRangeFilter;
        this.isNestedFilter = isNestedFilter;
        this.nestedPath = nestedPath;
        this.nestedFields = nestedFields;
        if (queryParam != null) this.tableParam = queryParam.getTableParam();
    }
}
