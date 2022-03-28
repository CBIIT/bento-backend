package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class YamlFilterType {
    private String type;
    private String selectedField;
    private String subAggSelectedField;
    private boolean filter;
    // TODO NOT NEEDED FOR SIZE
    private int size;
    // Global Query Sets
    private List<YamlGlobalFilterType.GlobalQuerySet> query;
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
