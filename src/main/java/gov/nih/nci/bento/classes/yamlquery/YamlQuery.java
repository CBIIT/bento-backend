package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class YamlQuery {

    private String name;
    private List<String> index;
    private String resultType;
    private YamlFilterType filterType;
    private HighlightQuery highlight;

}
