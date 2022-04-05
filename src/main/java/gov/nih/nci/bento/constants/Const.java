package gov.nih.nci.bento.constants;

public class Const {

    public static class GRAPHQL {
        public static final String QUERY_TYPE_NAME = "Query";
        public static final String MUTATION_TYPE_NAME = "Mutation";
        public static final String SUBSCRIPTION_TYPE_NAME = "Subscription";
    }

    public static class YAML_QUERY {

        public static class FILE_NAMES {
            public static final String SINGLE = "yaml/single_query.yml";
            public static final String GROUP = "yaml/group_query.yml";
            public static final String GLOBAL = "yaml/global_query.yml";
            public static final String TEST = "yaml/test_es_query.yml";

        }


        public static class QUERY_TERMS {
            public static final String TERM = "term";
            public static final String MATCH = "match";
            public static final String WILD_CARD = "wildcard";
            public static final String BOOLEAN = "boolean";
            public static final String INTEGER = "integer";
        }

        public static class FILTER {
            public static final String AGGREGATION = "aggregation";
            public static final String TABLE = "table";
            public static final String NO_OF_DOCUMENTS = "number_of_docs";
            public static final String RANGE = "range";
            public static final String SUB_AGGREAGATION = "sub_aggregation";
            public static final String DEFAULT = "default";
            public static final String GLOBAL = "global";
            public static final String NESTED = "nested";
        }

        public static class RESULT_TYPE {
            public static final String DEFAULT = "default";
            public static final String AGGREGATION = "aggregation";
            public static final String INT_TOTAL_AGGREGATION = "int_total_aggregation";
            public static final String RANGE = "range";
            public static final String ARM_PROGRAM = "arm_program";

            public static final String INT_TOTAL_COUNT = "int_total_count";
            public static final String STRING_LIST = "str_list";
            public static final String GLOBAL_ABOUT = "global_about";
            public static final String GLOBAL = "global";
            public static final String GLOBAL_MULTIPLE_MODEL = "global_multi_models";
            public static final String NESTED = "nested";
            public static final String NESTED_TOTAL = "nested_total";
            public static final String NESTED_LIST = "nested_list";
        }


    }


    public static class ES_UNITS {
        public static final int DEFAULT_SIZE = 10;
        public static final int MAX_SIZE = 10000;
        public static final String GS_HIGHLIGHT_DELIMITER = "$";
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
        public static final String FILE_ID = "file_id";
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
        public static final String SAMPLE_ID_NUM = "sample_id_num";
        public static final String SAMPLE_ID = "sample_id";
        public static final String FILE_NAME = "file_name";
        public static final String PAGE = "page";
        public static final String TITLE = "title";
        public static final String TYPE = "type";
        public static final String TEXT = "text";
        public static final String ABOUT = "about";
        public static final String CONTENT_PARAGRAPH = "content.paragraph";
        public static final String PROGRAM_ID = "program_id";
        public static final String PROGRAM_CODE = "program_code";
        public static final String PROGRAM_NAME = "program_name";
        public static final String SAMPLE_ANATOMIC_SITE = "sample_anatomic_site";
        public static final String SAMPLE_ID_GS = "sample_id_gs";
        public static final String SAMPLE_ANATOMIC_SITE_GS = "sample_anatomic_site_gs";
        public static final String TISSUE_TYPE_GS = "tissue_type_gs";
        public static final String STUDY_ID = "study_id";
        public static final String STUDY_TYPE = "study_type";
        public static final String STUDY_CODE = "study_code";
        public static final String STUDY_NAME = "study_name";
        public static final String NODE_NAME = "node_name";
        public static final String PROPERTY_NAME = "property_name";
        public static final String PROPERTY_TYPE = "property_type";
        public static final String PROPERTY_REQUIRED = "property_required";
        public static final String PROPERTY_DESCRIPTION = "property_description";
        public static final String FILE_FORMAT = "file_format";
        public static final String FILE_ID_GS = "file_id_gs";
        public static final String FILE_NAME_GS = "file_name_gs";
        public static final String FILE_FORMAT_GS = "file_format_gs";
        public static final String SUBJECT_ID_GS = "subject_id_gs";
        public static final String DIGNOSIS_GS = "diagnosis_gs";
        public static final String VALUE = "value";
        public static final String HIGHLIGHT = "highlight";
        public static final String FILE_ID_NUM = "file_id_num";
        public static final String STUDY_ID_KW = "study_id_kw";
        public static final String PROGRAM_ID_KW = "program_id_kw";
        public static final String AGE = "age";
        public static final String PROGRAM_KW = "property_kw";
        public static final String SURVIVAL_TIME = "survival_time";
        public static final String GLOBAL_SEARCH_SUBJECTS = "global_search_subjects";
        public static final String GLOBAL_SEARCH_PROGRAM = "global_search_program";
        public static final String GLOBAL_SEARCH_STUDIES = "global_search_studies";
        public static final String GLOBAL_SEARCH_SAMPLE = "global_search_sample";
        public static final String GLOBAL_SEARCH_FILE = "global_search_file";
        public static final String GLOBAL_SEARCH_MODEL = "global_search_model";
        public static final String GLOBAL_SEARCH_ABOUT = "global_search_about";
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
        public static final String MODEL_NODES = "model_nodes";
        public static final String MODEL_PROPERTIES = "model_properties";
        public static final String ABOUT = "about_page";
        public static final String MODEL_VALUES = "model_values";
    }
}
