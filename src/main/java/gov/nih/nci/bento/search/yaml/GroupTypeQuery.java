package gov.nih.nci.bento.search.yaml;

import gov.nih.nci.bento.search.yaml.filter.YamlQuery;
import lombok.Data;

import java.util.List;

@Data
public class GroupTypeQuery {

    private List<Group> groups;

    @Data
    public static class Group {
        private String name;
        private List<YamlQuery> query;
    }
}