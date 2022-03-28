package gov.nih.nci.bento.search.query.yaml.filter;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class YamlHighlight {

    private List<String> fields;
    private String preTag;
    private String postTag;
    private int fragmentSize;
}