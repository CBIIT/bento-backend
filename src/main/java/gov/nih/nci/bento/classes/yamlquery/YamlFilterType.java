package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class YamlFilterType {
    private String type;
    private String selectedField;
    private String subAggSelectedField;
    private boolean filter;
    // TODO NOT NEEDED FOR SIZE
    private int size;
}
