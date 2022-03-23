package gov.nih.nci.bento.classes;

import lombok.Data;

import java.util.List;

@Data
public class GroupQuery {
    private String name;
    private List<Query> query;

    @Data
    public static class Query {
        private String name;
        private String index;
        private String filterType;
        private ResultMapper resultType;
    }

    @Data
    public static class ResultMapper {
        private String type;
        private String selectedField;
    }
}