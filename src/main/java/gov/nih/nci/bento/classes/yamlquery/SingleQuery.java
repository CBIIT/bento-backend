package gov.nih.nci.bento.classes.yamlquery;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class SingleQuery {

    private List<YamlQuery> query;
}