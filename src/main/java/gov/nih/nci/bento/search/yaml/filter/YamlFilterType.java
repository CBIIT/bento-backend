package gov.nih.nci.bento.search.yaml.filter;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class YamlFilterType {
    private String type;
    private String defaultSortField;
    private String selectedField;
    private String subAggSelectedField;
    private String nestedPath;
    private Set<String> nestedFields;
    private String sortDirection;
    private boolean filter;
    private boolean caseInSensitive;
    private int size;
    private Map<String, String> prioritySort;
    private Set<String> returnAllFields = new HashSet<>();

    // Global Query Sets
    private List<YamlGlobalFilterType.GlobalQuerySet> query;
    // Desired type search query; boolean, integer
    private List<YamlGlobalFilterType.GlobalQuerySet> optionalQuery;

    @Data
    public static  class GlobalQuerySet {
        private String field;
        private String type;
        private String option;
        private boolean caseInSensitive;
    }
}
