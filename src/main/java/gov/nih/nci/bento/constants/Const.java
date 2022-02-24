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

    public static class ES_UNITS {
        public static final int DEFAULT_SIZE = 10000;
    }

    // TODO MOVE TO PRIVATE
    public static class ES_KEYS {
        public static final String NO_OF_PROGRAMS = "numberOfPrograms";
        public static final String NO_OF_STUDIES = "numberOfStudies";
        public static final String NO_OF_PROCEDURES = "numberOfLabProcedures";
        public static final String NO_OF_SUBJECTS = "numberOfSubjects";
        public static final String NO_OF_SAMPLES = "numberOfSamples";
        public static final String NO_OF_FILES = "numberOfFiles";
        public static final String NO_OF_ARMS_PROGRAM = "armsByPrograms";
    }

    public static class ES_PARAMS {
        public static final String PAGE_SIZE = "first";
        public static final String OFFSET = "offset";
        public static final String ORDER_BY = "order_by";
        public static final String TERMS_AGGS = "terms_aggs";

        public static final String CASE_IDS = "case_ids";
    }

    public static class ES_FIELDS {
        public static final String CLINICAL_STUDY = "clinical_study_designation";
        public static final String CASE_ID = "case_id";
        public static final String FILE_NAME = "file_name";
        public static final String CASE_MEMBER_OF_STUDY = "case_ids_case_to_member_of_to_study";
        public static final String DIAG_OF_CASE_CASE = "case_id_diag_to_case_of_to_case";
        public static final String DEMOG_OF_CASE_CASE = "case_id_demogr_to_case_of_to_case";
        public static final String DIAG_CASE_OF_CASE = "case_id_diag_to_case_of_to_case";

        public static final String COUNT = "count";
        public static final String GROUP = "group";
        public static final String SEX = "sex";
        public static final String SAMPLE_ID = "sample_id";
        public static final String DISEASE_TERM = "disease_term";
        public static final String BREED = "breed";
        public static final String NEUTERED_INDICATOR = "neutered_indicator";
        public static final String STAGE_OF_DISEASE = "stage_of_disease";
        public static final String PRIMARY_DISEASE_SITE = "primary_disease_site";
    }

    public static class ES_INDEX {
        public static final String SAMPLES = "samples";
        public static final String FILES = "files";
        public static final String CASES = "cases";
        public static final String STUDIES = "studies";
        public static final String DEMOGRAPHIC = "demographic";
        public static final String DIAGNOSIS = "diagnosis";
    }


}
