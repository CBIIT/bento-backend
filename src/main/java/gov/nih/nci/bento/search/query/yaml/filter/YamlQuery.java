package gov.nih.nci.bento.search.query.yaml.filter;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class YamlQuery {

    private String name;
    private String[] index;
    private String resultType;
    private YamlFilterType filterType;
    private YamlHighlight highlight;
}
