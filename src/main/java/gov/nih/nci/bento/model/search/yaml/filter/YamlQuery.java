package gov.nih.nci.bento.model.search.yaml.filter;

import lombok.Data;

@Data
public class YamlQuery {

    private String name;
    private String[] index;
    private YamlResult result;
    private YamlFilter filter;
//    private YamlDynamicFilter dynamicFilter;
//    private YamlHighlight highlight;
}
