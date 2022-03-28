package gov.nih.nci.bento.search.query.yaml.filter;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class YamlGlobalFilterType {
    private String type;
    private String selectedField;
    private List<GlobalQuerySet> query;
    private List<GlobalQuerySet> optionalQuery;

    @Data
    @Getter
    public static  class GlobalQuerySet {
        private String field;
        private String type;
        private String option;
        private boolean caseInSensitive;
    }
}
