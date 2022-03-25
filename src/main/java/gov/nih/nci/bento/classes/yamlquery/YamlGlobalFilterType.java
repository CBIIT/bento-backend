package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class YamlGlobalFilterType {
    private List<GlobalQuerySet> query;
    private String selectedField;
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
