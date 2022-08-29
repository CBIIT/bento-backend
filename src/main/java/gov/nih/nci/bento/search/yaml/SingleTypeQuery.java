package gov.nih.nci.bento.search.yaml;

import gov.nih.nci.bento.search.yaml.filter.YamlQuery;
import lombok.Data;

import java.util.List;

@Data
public class SingleTypeQuery {

    private List<YamlQuery> queries;
}