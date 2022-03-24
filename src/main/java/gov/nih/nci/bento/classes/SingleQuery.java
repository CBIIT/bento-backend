package gov.nih.nci.bento.classes;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class SingleQuery {

    private List<YamlQuery> query;

    @Data
    @Getter
    public static class FilterType {
        private String type;
        private String selectedField;
        private int size;
    }
}