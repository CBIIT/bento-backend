package gov.nih.nci.bento.model;

import com.google.gson.*;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.Request;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class IcdcEsFilter implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(IcdcEsFilter.class);

    // parameters used in queries
    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";
    final String SORT_DIRECTION = "sort_direction";

    final String PROGRAMS_END_POINT = "/programs/_search";
    final String PROGRAMS_COUNT_END_POINT = "/programs/_count";
    final String STUDIES_END_POINT = "/studies/_search";
    final String STUDIES_COUNT_END_POINT = "/studies/_count";

    final String CASES_END_POINT = "/cases/_search";
    final String CASES_COUNT_END_POINT = "/cases/_count";
    final String SAMPLES_END_POINT = "/samples/_search";
    final String SAMPLES_COUNT_END_POINT = "/samples/_count";
    final String FILES_END_POINT = "/files/_search";
    final String FILES_COUNT_END_POINT = "/files/_count";
    final String NODES_END_POINT = "/model_nodes/_search";
    final String NODES_COUNT_END_POINT = "/model_nodes/_count";
    final String PROPERTIES_END_POINT = "/model_properties/_search";
    final String PROPERTIES_COUNT_END_POINT = "/model_properties/_count";
    final String VALUES_END_POINT = "/model_values/_search";
    final String VALUES_COUNT_END_POINT = "/model_values/_count";
    final String GS_ABOUT_END_POINT = "/about_page/_search";
    final String GS_MODEL_END_POINT = "/data_model/_search";

    final int GS_LIMIT = 10;
    final String GS_END_POINT = "endpoint";
    final String GS_COUNT_ENDPOINT = "count_endpoint";
    final String GS_RESULT_FIELD = "result_field";
    final String GS_COUNT_RESULT_FIELD = "count_result_field";
    final String GS_SEARCH_FIELD = "search_field";
    final String GS_COLLECT_FIELDS = "collect_fields";
    final String GS_SORT_FIELD = "sort_field";
    final String GS_CATEGORY_TYPE = "type";
    final String GS_ABOUT = "about";
    final String GS_HIGHLIGHT_FIELDS = "highlight_fields";
    final String GS_HIGHLIGHT_DELIMITER = "$";
    final Set<String> RANGE_PARAMS = Set.of("age_at_index");


    @Autowired
    ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("caseOverviewPaged", env -> {
                            Map<String, Object> args = env.getArguments();
                            return caseOverview(args, "asc");
                        })
                        .dataFetcher("caseOverviewPagedDesc", env -> {
                            Map<String, Object> args = env.getArguments();
                            return caseOverview(args, "desc");
                        })
                        .dataFetcher("sampleOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return sampleOverview(args, "asc");
                        })
                        .dataFetcher("sampleOverviewDesc", env -> {
                            Map<String, Object> args = env.getArguments();
                            return sampleOverview(args, "desc");
                        })
                        .dataFetcher("fileOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileOverview(args, "asc");
                        })
                        .dataFetcher("fileOverviewDesc", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileOverview(args, "desc");
                        })
                )
                .build();
    }

    private List<Map<String, Object>> caseOverview(Map<String, Object> params, String sortDirection) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"case_id", "case_ids"},
                new String[]{"study_code", "study"},
                new String[]{"study_type", "study_type"},
                new String[]{"cohort", "cohort"},
                new String[]{"breed", "breed"},
                new String[]{"diagnosis", "diagnosis"},
                new String[]{"stage_of_disease", "stage_of_disease"},
                new String[]{"age", "age"},
                new String[]{"sex", "sex"},
                new String[]{"neutered_status", "neutered_status"},
                new String[]{"weight", "weight"},
                new String[]{"response_to_treatment", "response_to_treatment"},
                new String[]{"disease_site", "disease_site"},
                new String[]{"files", "files"},
                new String[]{"other_cases", "other_cases"},
                new String[]{"individual_id", "individual_id"},
                new String[]{"primary_disease_site", "disease_site"},
                new String[]{"date_of_diagnosis", "date_of_diagnosis"},
                new String[]{"histology_cytopathology", "histology_cytopathology"},
                new String[]{"histological_grade", "histological_grade"},
                new String[]{"pathology_report", "pathology_report"},
                new String[]{"treatment_data", "treatment_data"},
                new String[]{"follow_up_data", "follow_up_data"},
                new String[]{"concurrent_disease", "concurrent_disease"},
                new String[]{"concurrent_disease_type", "concurrent_disease_type"},
                new String[]{"arm", "arm"}
        };

        String defaultSort = "case_ids"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("study_code", "study"),
                Map.entry("study_type", "study_type"),
                Map.entry("cohort", "cohort"),
                Map.entry("breed", "breed"),
                Map.entry("diagnosis", "diagnosis"),
                Map.entry("stage_of_disease", "stage_of_disease"),
                Map.entry("disease_site", "disease_site"),
                Map.entry("age", "age"),
                Map.entry("sex", "sex"),
                Map.entry("neutered_status", "neutered_status"),
                Map.entry("weight", "weight"),
                Map.entry("response_to_treatment", "response_to_treatment"),
                Map.entry("other_cases", "other_cases"),
                Map.entry("case_id", "case_ids")
        );

        Map<String, Object> newParams = new HashMap<>(params);
        newParams.put(SORT_DIRECTION, sortDirection);

        return overview(CASES_END_POINT, newParams, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> sampleOverview(Map<String, Object> params, String sortDirection) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"sample_id", "sample_ids"},
                new String[]{"case_id", "case_ids"},
                new String[]{"breed", "breed"},
                new String[]{"diagnosis", "diagnosis"},
                new String[]{"sample_site", "sample_site"},
                new String[]{"sample_type", "sample_type"},
                new String[]{"sample_pathology", "sample_pathology"},
                new String[]{"tumor_grade", "tumor_grade"},
                new String[]{"sample_chronology", "sample_chronology"},
                new String[]{"percentage_tumor", "percentage_tumor"},
                new String[]{"necropsy_sample", "necropsy_sample"},
                new String[]{"sample_preservation", "sample_preservation"},
                new String[]{"files", "files"},
                new String[]{"physical_sample_type", "physical_sample_type"},
                new String[]{"general_sample_pathology", "general_sample_pathology"},
                new String[]{"tumor_sample_origin", "tumor_sample_origin"},
                new String[]{"comment", "comment"},
                new String[]{"individual_id", "individual_id"},
                new String[]{"other_cases", "other_cases"},
                new String[]{"patient_age_at_enrollment", "patient_age_at_enrollment"},
                new String[]{"sex", "sex"},
                new String[]{"neutered_indicator", "neutered_indicator"},
                new String[]{"weight", "weight"},
                new String[]{"primary_disease_site", "primary_disease_site"},
                new String[]{"stage_of_disease", "stage_of_disease"},
                new String[]{"date_of_diagnosis", "date_of_diagnosis"},
                new String[]{"histology_cytopathology", "histology_cytopathology"},
                new String[]{"histological_grade", "histological_grade"},
                new String[]{"best_response", "best_response"},
                new String[]{"pathology_report", "pathology_report"},
                new String[]{"treatment_data", "treatment_data"},
                new String[]{"follow_up_data", "follow_up_data"},
                new String[]{"concurrent_disease", "concurrent_disease"},
                new String[]{"concurrent_disease_type", "concurrent_disease_type"},
                new String[]{"cohort_description", "cohort_description"},
                new String[]{"arm", "arm"}
        };

        String defaultSort = "sample_ids"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("sample_id", "sample_ids"),
                Map.entry("case_id", "case_id"),
                Map.entry("breed", "breed"),
                Map.entry("diagnosis", "diagnosis"),
                Map.entry("sample_site", "sample_site"),
                Map.entry("sample_type", "sample_type"),
                Map.entry("sample_pathology", "sample_pathology"),
                Map.entry("tumor_grade", "tumor_grade"),
                Map.entry("sample_chronology", "sample_chronology"),
                Map.entry("percentage_tumor", "percentage_tumor"),
                Map.entry("necropsy_sample", "necropsy_sample"),
                Map.entry("sample_preservation", "sample_preservation")
        );

        Map<String, Object> newParams = new HashMap<>(params);
        newParams.put(SORT_DIRECTION, sortDirection);

        return overview(SAMPLES_END_POINT, newParams, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params, String sortDirection) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        final String[][] PROPERTIES = new String[][]{
                new String[]{"file_name", "file_name"},
                new String[]{"file_type", "file_type"},
                new String[]{"association", "parent_type"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"case_id", "case_ids"},
                new String[]{"breed", "breed"},
                new String[]{"diagnosis", "diagnosis"},
                new String[]{"study_code", "study_code"},
                new String[]{"file_uuid", "file_uuids"},
                new String[]{"sample_id", "sample_ids"},
                new String[]{"sample_site", "sample_site"},
                new String[]{"physical_sample_type", "physical_sample_type"},
                new String[]{"general_sample_pathology", "general_sample_pathology"},
                new String[]{"tumor_sample_origin", "tumor_sample_origin"},
                new String[]{"summarized_sample_type", "summarized_sample_type"},
                new String[]{"specific_sample_pathology", "specific_sample_pathology"},
                new String[]{"date_of_sample_collection", "date_of_sample_collection"},
                new String[]{"tumor_grade", "tumor_grade"},
                new String[]{"sample_chronology", "sample_chronology"},
                new String[]{"percentage_tumor", "percentage_tumor"},
                new String[]{"necropsy_sample", "necropsy_sample"},
                new String[]{"sample_preservation", "sample_preservation"},
                new String[]{"comment", "comment"},
                new String[]{"individual_id", "individual_id"},
                new String[]{"patient_age_at_enrollment", "patient_age_at_enrollment"},
                new String[]{"sex", "sex"},
                new String[]{"neutered_indicator", "neutered_indicator"},
                new String[]{"weight", "weight"},
                new String[]{"primary_disease_site", "primary_disease_site"},
                new String[]{"stage_of_disease", "stage_of_disease"},
                new String[]{"date_of_diagnosis", "date_of_diagnosis"},
                new String[]{"histology_cytopathology", "histology_cytopathology"},
                new String[]{"histological_grade", "histological_grade"},
                new String[]{"best_response", "best_response"},
                new String[]{"pathology_report", "pathology_report"},
                new String[]{"treatment_data", "treatment_data"},
                new String[]{"follow_up_data", "follow_up_data"},
                new String[]{"concurrent_disease", "concurrent_disease"},
                new String[]{"concurrent_disease_type", "concurrent_disease_type"},
                new String[]{"cohort_description", "cohort_description"},
                new String[]{"arm", "arm"},
                new String[]{"other_cases", "other_cases"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("file_name", "file_name"),
                Map.entry("file_type", "file_type"),
                Map.entry("association", "parent_type"),
                Map.entry("file_description", "file_description"),
                Map.entry("file_format", "file_format"),
                Map.entry("file_size", "file_size"),
                Map.entry("case_id", "case_id"),
                Map.entry("breed", "breed"),
                Map.entry("diagnosis", "diagnosis"),
                Map.entry("study_code", "study_code"),
                Map.entry("file_uuid", "file_uuid"),
                Map.entry("access_file", "file_size")
        );

        Map<String, Object> newParams = new HashMap<>(params);
        newParams.put(SORT_DIRECTION, sortDirection);

        return overview(FILES_END_POINT, newParams, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> overview(String endpoint, Map<String, Object> params, String[][] properties, String defaultSort, Map<String, String> mapping) throws IOException {

        Request request = new Request("GET", endpoint);
        Map<String, Object> query = esService.buildListQuery(params, Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
        String order_by = (String)params.get(ORDER_BY);
        String direction = ((String)params.get(SORT_DIRECTION)).toLowerCase();
        query.put("sort", mapSortOrder(order_by, direction, defaultSort, mapping));
        int pageSize = (int) params.get(PAGE_SIZE);
        int offset = (int) params.get(OFFSET);
        List<Map<String, Object>> page = esService.collectPage(request, query, properties, pageSize, offset);
        return page;
    }

    private Map<String, String> mapSortOrder(String order_by, String direction, String defaultSort, Map<String, String> mapping) {
        String sortDirection = direction;
        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            sortDirection = "asc";
        }

        String sortOrder = defaultSort; // Default sort order
        if (mapping.containsKey(order_by)) {
            sortOrder = mapping.get(order_by);
        } else {
            logger.info("Order: \"" + order_by + "\" not recognized, use default order");
        }
        return Map.of(sortOrder, sortDirection);
    }
}
