package gov.nih.nci.bento.search.query.yaml;

import gov.nih.nci.bento.search.query.yaml.filter.YamlQuery;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class GroupTypeQuery {

    private List<Group> groups;

    @Data
    @Getter
    public static class Group {
        private String name;
        private List<YamlQuery> query;
    }
}