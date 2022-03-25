package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class HighlightQuery {

    private String field;
    private String preTag;
    private String postTag;
    private int fragmentSize;
}