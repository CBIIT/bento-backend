package gov.nih.nci.bento.classes;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class GroupQuery {

    private List<Group> groups;

    @Data
    @Getter
    public static class Group {
        private String name;
        private List<YamlQuery> query;
    }
}