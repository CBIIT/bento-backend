package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class HighlightQuery {

    private List<String> fields;
    private String preTag;
    private String postTag;
    private int fragmentSize;
}