package gov.nih.nci.bento.test;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class YamlTest {

    private List<YamlTestQuery> query;

    @Data
    public static class YamlTestQuery {
        private String name;
        private Object request;
        private Set<String> expectedKeySet;
    }

}
