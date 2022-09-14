package gov.nih.nci.bento.constants;

public class Const {

    public static class ES_UNITS {
        public static final int DEFAULT_SIZE = 10;
        public static final int MAX_SIZE = 10000;
        public static final String GS_HIGHLIGHT_DELIMITER = "$";
        public static final String KEYWORD = ".keyword";
    }

    public static class ES_PARAMS {
        public static final String PAGE_SIZE = "first";
        public static final String OFFSET = "offset";
        public static final String ORDER_BY = "order_by";
        public static final String TERMS_AGGS = "terms_aggs";
        public static final String NESTED_SEARCH = "nested_search";

        public static final String SORT_DIRECTION = "sort_direction";
        public static final String CASE_IDS = "case_ids";
        public static final int AGGS_SIZE = 1000;
        public static final String INPUT = "input";
        public static final String NESTED_FILTER = "FILTER_INFO";
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
