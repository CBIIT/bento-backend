package gov.nih.nci.bento.search.yaml.filter;

import lombok.Data;

@Data
public class YamlQuery {

    private String name;
    private String[] index;
    private String resultType;
    private YamlFilter filter;
    private YamlDynamicFilter dynamicFilter;
    private YamlHighlight highlight;
}
