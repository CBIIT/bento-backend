package gov.nih.nci.bento.search.yaml.filter;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class YamlFilter {
    private String type;
    private String defaultSortField;
    private String selectedField;
    private String subAggSelectedField;
    private String nestedPath;
    private Set<String> nestedParameters;
    private String sortDirection;
    private String method;
    private boolean ignoreSelectedField;
    private boolean caseInsensitive;
    private int size;
    private Map<String, String> alternativeSort;
    private Set<String> returnAllParameters = new HashSet<>();

    // Global Query Sets
    private List<YamlGlobalFilterType.GlobalQuerySet> query;
    // Desired type search query; boolean, integer
    private List<YamlGlobalFilterType.GlobalQuerySet> typeQuery;

    @Data
    public static  class GlobalQuerySet {
        private String field;
        private String type;
        private String option;
        private boolean caseInsensitive;
    }
}
