package gov.nih.nci.bento.model.search.filter;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class FilterParam {

    private final Map<String, Object> args;
    private final String selectedField;

    @Builder
    public FilterParam(Map<String, Object> args, String selectedField) {
        this.args = args;
        this.selectedField = selectedField;
    }
}