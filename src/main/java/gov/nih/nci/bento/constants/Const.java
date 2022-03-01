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
        public static final String SORT_DIRECTION = "sort_direction";
        public static final String CASE_IDS = "case_ids";
        public static final int AGGS_SIZE = 1000;
    }

    public static class ICDC_FIELDS {
        public static final String CLINICAL_STUDY = "clinical_study_designation";
        public static final String CASE_ID = "case_id";
        public static final String FILE_NAME = "file_name";
        public static final String FILE_TYPE = "file_type";
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

    public static class ICDC_INDEX {
        public static final String SAMPLES = "samples";
        public static final String FILES = "files";
        public static final String CASES = "cases";
        public static final String STUDIES = "studies";
        public static final String DEMOGRAPHIC = "demographic";
        public static final String DIAGNOSIS = "diagnosis";
    }

    public static class BENTO_FIELDS {
        public static final String STUDIES = "study_info";
//        public static final String PROGRAMS = "programs";
        public static final String DIAGNOSES = "diagnosis";
        public static final String RC_SCORES = "recurrence_score";
        public static final String TUMOR_SIZES = "tumor_size";
        public static final String TUMOR_GRADES = "tumor_grade";
        public static final String ER_STATUS = "er_status";
        public static final String PR_STATUS = "pr_status";
        public static final String CHEMO_REGIMEN = "chemotherapy";
        public static final String ENDO_THERAPIES = "endocrine_therapy";
        public static final String MENO_STATUS = "menopause_status";
        public static final String TISSUE_TYPE = "tissue_type";
        public static final String COMPOSITION = "composition";
        public static final String ASSOCIATION = "association";
        public static final String FILE_TYPE = "file_type";
        public static final String LAB_PROCEDURES = "lab_procedures";
        public static final String SUBJECT_ID = "subject_id";
        public static final String AGE_AT_INDEX = "age_at_index";

        public static final String LOWER_BOUND = "lowerBound";
        public static final String UPPER_BOUND = "upperBound";
        public static final String SUBJECTS = "subjects";

        public static final String COUNT = "count";
        public static final String GROUP = "group";
        public static final String PROGRAM = "program";
        public static final String CASE_SIZE = "caseSize";
        public static final String CHILDREN = "children";
        public static final String ARM = "arm";
        public static final String SIZE = "size";
        // Sort Purpose
        public static final String SUBJECT_ID_NUM = "subject_id_num";
        public static final String STUDY_ACRONYM = "study_acronym";
    }

    public static class BENTO_INDEX {
        public static final String SUBJECTS = "subjects";
        public static final String FILES = "files";
        public static final String STUDIES = "studies";
        public static final String SAMPLES = "samples";
        public static final String REGISTRATION = "registration";
        public static final String PROGRAMS = "programs";
        public static final String PROGRAM = "program";
        public static final String CASES = "cases";
    }


}
