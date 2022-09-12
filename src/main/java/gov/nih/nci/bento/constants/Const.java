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

    public static class BENTO_INDEX {
        public static final String SUBJECTS = "subjects";
    }
}
