package gov.nih.nci.bento.search.yaml.filter;

import lombok.Data;

@Data
public class YamlDynamicFilter {

    private String[] index;
    private String dynamicField;
    private String targetField;
}
