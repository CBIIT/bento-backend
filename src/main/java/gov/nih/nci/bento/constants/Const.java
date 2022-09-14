package gov.nih.nci.bento.constants;

public class Const {

    public static class ES_UNITS {
        public static final int MAX_SIZE = 10000;
    }

    public static class ES_PARAMS {
        public static final String INPUT = "input";
    }

    public static class YAML_QUERY {
        public static class FILE_NAMES_BENTO {
            public static final String SINGLE = "yaml/single_search_es.yml";
//            public static final String GROUP = "yaml/facet_search_es.yml";
//            public static final String GLOBAL = "yaml/global_search_es.yml";
        }

        public static class FILTER {
            public static final String DEFAULT = "default";
        }

        public static class RESULT_TYPE {
            public static final String OBJECT_ARRAY = "object_array";
        }
    }

    public static class BENTO_INDEX {
        public static final String SUBJECTS = "subjects";
    }
}
