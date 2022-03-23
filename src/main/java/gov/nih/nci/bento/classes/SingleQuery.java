package gov.nih.nci.bento.classes;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class SingleQuery {

    private List<Query> query;

    @Data
    @Getter
    public static class Query {
        private String name;
        private String index;
        private String resultType;
        private FilterType filterType;
    }

    @Data
    @Getter
    public static class FilterType {
        private String type;
        private String selectedField;
    }
}