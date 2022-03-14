package gov.nih.nci.bento.constants;

import java.util.HashMap;
import java.util.Map;

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
    }

    public static class BENTO_INDEX {
        public static final String SUBJECTS = "subjects";
        public static final String FILES = "files";
        // TODO TOBE DELETED
        public static final String FILES_TEST = "files_test";
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

    // TODO temporary use
    public static Map<String, String> getTempQueryParamMap() {
        Map<String, String> keyMap= new HashMap<>();
        // Subject Index
        keyMap.put("diagnoses", "diagnosis" + ES_UNITS.KEYWORD);
        keyMap.put("rc_scores", Const.BENTO_FIELDS.RC_SCORES + ES_UNITS.KEYWORD);
        keyMap.put("tumor_sizes", "tumor_size" + ES_UNITS.KEYWORD);
        keyMap.put("chemo_regimen", "chemotherapy" + ES_UNITS.KEYWORD);
        keyMap.put("tumor_grades", "tumor_grade" + ES_UNITS.KEYWORD);
        keyMap.put("subject_ids", "subject_id" + ES_UNITS.KEYWORD);
        keyMap.put("studies", "study_info" + ES_UNITS.KEYWORD);
        keyMap.put("meno_status", "menopause_status" + ES_UNITS.KEYWORD);
        keyMap.put("programs", "program" + Const.ES_UNITS.KEYWORD);
        keyMap.put("er_status", "er_status" + Const.ES_UNITS.KEYWORD);
        keyMap.put("pr_status", "pr_status" + Const.ES_UNITS.KEYWORD);
        keyMap.put("endo_therapies", "endocrine_therapy" + Const.ES_UNITS.KEYWORD);
        keyMap.put("tissue_type", "tissue_type" + Const.ES_UNITS.KEYWORD);
        keyMap.put("composition", "composition" + Const.ES_UNITS.KEYWORD);
        keyMap.put("association", "association" + Const.ES_UNITS.KEYWORD);
        keyMap.put("file_type", "file_type" + Const.ES_UNITS.KEYWORD);
        keyMap.put("age_at_index", "age_at_index");

        // Files Index
        keyMap.put("file_ids", "file_id" + Const.ES_UNITS.KEYWORD);
        keyMap.put("file_names", "file_name" + Const.ES_UNITS.KEYWORD);
        keyMap.put("sample_ids", "sample_id" + Const.ES_UNITS.KEYWORD);
        return keyMap;
    }

    public static Map<String, String> getOppositeTempQueryParamMap() {
        Map<String, String> keyMap= new HashMap<>();
        // Subject Index
        keyMap.put("diagnosis" + Const.ES_UNITS.KEYWORD, "diagnoses");
        keyMap.put(Const.BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD, "rc_scores");
        keyMap.put("tumor_size" + Const.ES_UNITS.KEYWORD, "tumor_sizes");
        keyMap.put("chemotherapy" + Const.ES_UNITS.KEYWORD, "chemo_regimen");
        keyMap.put("tumor_grade" + Const.ES_UNITS.KEYWORD, "tumor_grades");
        keyMap.put("subject_id" + Const.ES_UNITS.KEYWORD, "subject_ids");
        keyMap.put("study_info" + Const.ES_UNITS.KEYWORD, "studies");
        keyMap.put("menopause_status" + Const.ES_UNITS.KEYWORD, "meno_status");
        keyMap.put("program" + Const.ES_UNITS.KEYWORD, "programs");
        keyMap.put("er_status" + Const.ES_UNITS.KEYWORD, "er_status");
        keyMap.put("pr_status" + Const.ES_UNITS.KEYWORD, "pr_status");
        keyMap.put("endocrine_therapy" + Const.ES_UNITS.KEYWORD ,"endo_therapies");
        keyMap.put("tissue_type" + Const.ES_UNITS.KEYWORD, "tissue_type");
        keyMap.put("composition" + Const.ES_UNITS.KEYWORD, "composition");
        keyMap.put("association" + Const.ES_UNITS.KEYWORD,"association");
        keyMap.put("file_type" + Const.ES_UNITS.KEYWORD,"file_type");
        keyMap.put("age_at_index", "age_at_index");

        // Files Index
        keyMap.put("file_id" + Const.ES_UNITS.KEYWORD,"file_ids");
        keyMap.put("file_name" + Const.ES_UNITS.KEYWORD,"file_names");
        keyMap.put("sample_id" + Const.ES_UNITS.KEYWORD,"sample_ids");
        return keyMap;
    }


}
