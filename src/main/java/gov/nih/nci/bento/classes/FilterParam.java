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

    @Builder
    @SuppressWarnings("unchecked")
    public FilterParam(Map<String, Object> args, String selectedField, String subAggSelectedField, boolean isExcludeFilter) {
        this.args = args;
        this.selectedField = selectedField;
        this.subAggSelectedField = subAggSelectedField;
        this.isExcludeFilter = isExcludeFilter;
    }
}
