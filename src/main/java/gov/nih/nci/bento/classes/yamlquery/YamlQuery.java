package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;
@Data
@Getter
public class YamlQuery {

    private String name;
    private String index;
    private String resultType;
    private YamlFilterType filterType;
    private YamlGlobalFilterType globalFilterType;
    private HighlightQuery highlight;

}
