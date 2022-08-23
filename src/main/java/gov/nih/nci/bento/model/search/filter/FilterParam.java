package gov.nih.nci.bento.model.search.filter;

import gov.nih.nci.bento.model.search.query.QueryParam;
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
    private final Set<String> nestedFields;
    private final Set<String> returnAllParameters;


    @Builder
    public FilterParam(Map<String, Object> args, String selectedField, String subAggSelectedField,
                       boolean isExcludeFilter, QueryParam queryParam, String defaultSortField, String customOrderBy,
                       boolean isRangeFilter, String nestedPath, Set<String> nestedFields,
                       boolean caseInsensitive, int size, Set<String> returnAllParameters, String sortDirection, TableParam tableParam) {
        this.args = args;
        this.selectedField = selectedField;
        this.subAggSelectedField = subAggSelectedField;
        this.isExcludeFilter = isExcludeFilter;
        this.queryParam = queryParam;
        this.defaultSortField = defaultSortField;
        this.customOrderBy = customOrderBy;
        this.isRangeFilter = isRangeFilter;
        this.nestedPath = nestedPath;
        this.nestedFields = nestedFields != null ? nestedFields : new HashSet<>();
        this.caseInsensitive = caseInsensitive;
        this.size = size;
        this.returnAllParameters = returnAllParameters;
        this.sortDirection = sortDirection;
//        if (queryParam != null) this.tableParam = queryParam.getTableParam();
        this.tableParam = tableParam;
    }
}
