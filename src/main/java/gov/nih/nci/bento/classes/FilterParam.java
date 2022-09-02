package gov.nih.nci.bento.classes;

import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
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
    private final String sortDirection;
    private final boolean isRangeFilter;
    private final boolean caseInsensitive;
    private final int size;
    private TableParam tableParam;
    private final String nestedPath;
    private final Set<String> nestedParameters;
    private final Set<String> ignoreIfEmpty;


    @Builder
    public FilterParam(Map<String, Object> args, String selectedField, String subAggSelectedField,
                       boolean isExcludeFilter, QueryParam queryParam, String defaultSortField, String customOrderBy,
                       boolean isRangeFilter, String nestedPath, Set<String> nestedParameters,
                       boolean caseInsensitive, int size, Set<String> ignoreIfEmpty, String sortDirection) {
        this.args = args;
        this.selectedField = selectedField;
        this.subAggSelectedField = subAggSelectedField;
        this.isExcludeFilter = isExcludeFilter;
        this.queryParam = queryParam;
        this.defaultSortField = defaultSortField;
        this.customOrderBy = customOrderBy;
        this.isRangeFilter = isRangeFilter;
        this.nestedPath = nestedPath;
        this.nestedParameters = nestedParameters != null ? nestedParameters : new HashSet<>();
        this.caseInsensitive = caseInsensitive;
        this.size = size;
        this.ignoreIfEmpty = ignoreIfEmpty;
        this.sortDirection = sortDirection;
        if (queryParam != null) this.tableParam = queryParam.getTableParam();
    }
}
