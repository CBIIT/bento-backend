package gov.nih.nci.bento.constants;

public class Const {

    public static class GRAPHQL {
        public static final String QUERY_TYPE_NAME = "Query";
        public static final String MUTATION_TYPE_NAME = "Mutation";
        public static final String SUBSCRIPTION_TYPE_NAME = "Subscription";
    }

    public static class ES_FILTER {
        public static final String AGG_NAME = "agg_name";
        public static final String AGG_ENDPOINT = "agg_endpoint";
        public static final String WIDGET_QUERY = "widgetQueryName";
        public static final String FILTER_COUNT_QUERY = "filterCountQueryName";
    }

    public static class ES_KEYS {
        public static final String NO_OF_PROGRAMS = "numberOfPrograms";
        public static final String NO_OF_STUDIES = "numberOfStudies";
        public static final String NO_OF_PROCEDURES = "numberOfLabProcedures";
        public static final String NO_OF_SUBJECTS = "numberOfSubjects";
        public static final String NO_OF_SAMPLES = "numberOfSamples";
        public static final String NO_OF_FILES = "numberOfFiles";
        public static final String NO_OF_ARMS_PROGRAM = "armsByPrograms";
    }

    public static class ES_TERMS {

    }

    public static class ES_INDEX {
        public static final String SAMPLES = "samples";
        public static final String FILES = "files";
        public static final String CASES = "cases";
        public static final String STUDIES = "studies";
    }


}
