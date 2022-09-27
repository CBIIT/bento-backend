package gov.nih.nci.bento.model.search.filter;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class FilterParam {

    private final Map<String, Object> args;
    private final String selectedField;
    private final Set<String> ignoreIfEmpty;
    private final boolean caseInsensitive;

    @Builder
    public FilterParam(Map<String, Object> args, String selectedField, Set<String> ignoreIfEmpty, boolean caseInsensitive) {
        this.args = args;
        this.selectedField = selectedField;
        this.ignoreIfEmpty = ignoreIfEmpty;
        this.caseInsensitive = caseInsensitive;
    }
}