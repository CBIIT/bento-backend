package gov.nih.nci.bento.search.yaml.filter;

import lombok.Data;

import java.util.List;

@Data
public class YamlGlobalFilterType {
    private String type;
    private String selectedField;
    private List<GlobalQuerySet> query;
    private List<GlobalQuerySet> optionalQuery;

    @Data
    public static  class GlobalQuerySet {
        private String field;
        private String type;
        private String option;
        private boolean caseInSensitive;
    }
}
