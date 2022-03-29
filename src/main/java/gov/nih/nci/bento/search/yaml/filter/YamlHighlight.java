package gov.nih.nci.bento.search.yaml.filter;

import lombok.Data;

import java.util.List;

@Data
public class YamlHighlight {

    private List<String> fields;
    private String preTag;
    private String postTag;
    private int fragmentSize;
}