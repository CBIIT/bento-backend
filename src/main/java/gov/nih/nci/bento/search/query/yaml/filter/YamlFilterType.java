package gov.nih.nci.bento.search.query.yaml.filter;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class YamlFilterType {
    private String type;
    private String defaultSortField;
    private String selectedField;
    private String subAggSelectedField;
    private boolean filter;
    // Global Query Sets
    private List<YamlGlobalFilterType.GlobalQuerySet> query;
    // Desired type search query; boolean, integer
    private List<YamlGlobalFilterType.GlobalQuerySet> optionalQuery;

    @Data
    @Getter
    public static  class GlobalQuerySet {
        private String field;
        private String type;
        private String option;
        private boolean caseInSensitive;
    }
}
