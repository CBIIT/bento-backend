package gov.nih.nci.bento.search.yaml;

import gov.nih.nci.bento.search.yaml.filter.YamlQuery;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class SingleTypeQuery {

    private List<YamlQuery> query;
}