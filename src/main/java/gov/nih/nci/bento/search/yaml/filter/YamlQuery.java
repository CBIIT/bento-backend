package gov.nih.nci.bento.search.yaml.filter;

import lombok.Data;

@Data
public class YamlQuery {

    private String name;
    private String[] index;
    private String resultType;
    private YamlFilterType filterType;
    private YamlHighlight highlight;
}
