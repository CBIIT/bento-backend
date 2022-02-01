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

    final String SUBJECTS_END_POINT = "/subjects/_search";
    final String SUBJECTS_COUNT_END_POINT = "/subjects/_count";
    final String SUBJECT_IDS_END_POINT = "/subject_ids/_search";
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
                new String[]{"subject_id", "subject_ids"},
                new String[]{"program", "programs"},
                new String[]{"program_id", "program_id"},
                new String[]{"study_acronym", "study_acronym"},
                new String[]{"study_short_description", "study_short_description"},
                new String[]{"study_info", "studies"},
                new String[]{"diagnosis", "diagnoses"},
                new String[]{"recurrence_score", "rc_scores"},
                new String[]{"tumor_size", "tumor_sizes"},
                new String[]{"tumor_grade", "tumor_grades"},
                new String[]{"er_status", "er_status"},
                new String[]{"pr_status", "pr_status"},
                new String[]{"chemotherapy", "chemo_regimen"},
                new String[]{"endocrine_therapy", "endo_therapies"},
                new String[]{"menopause_status", "meno_status"},
                new String[]{"age_at_index", "age_at_index"},
                new String[]{"survival_time", "survival_time"},
                new String[]{"survival_time_unit", "survival_time_unit"},
                new String[]{"files", "files"},
                new String[]{"samples", "samples"},
                new String[]{"lab_procedures", "lab_procedures"},
        };

        String defaultSort = "subject_id_num"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("subject_id", "subject_id_num"),
                Map.entry("program", "programs"),
                Map.entry("program_id", "program_id"),
                Map.entry("study_acronym", "study_acronym"),
                Map.entry("study_short_description", "study_short_description"),
                Map.entry("study_info", "studies"),
                Map.entry("diagnosis", "diagnoses"),
                Map.entry("recurrence_score", "rc_scores"),
                Map.entry("tumor_size", "tumor_sizes"),
                Map.entry("tumor_grade", "tumor_grades"),
                Map.entry("er_status", "er_status"),
                Map.entry("pr_status", "pr_status"),
                Map.entry("chemotherapy", "chemo_regimen"),
                Map.entry("endocrine_therapy", "endo_therapies"),
                Map.entry("menopause_status", "meno_status"),
                Map.entry("age_at_index", "age_at_index"),
                Map.entry("survival_time", "survival_time")
        );

        params.put(SORT_DIRECTION, sortDirection);

        return overview(SUBJECTS_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> sampleOverview(Map<String, Object> params, String sortDirection) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"program", "programs"},
                new String[]{"program_id", "program_id"},
                new String[]{"arm", "study_acronym"},
                new String[]{"subject_id", "subject_ids"},
                new String[]{"sample_id", "sample_ids"},
                new String[]{"diagnosis", "diagnoses"},
                new String[]{"tissue_type", "tissue_type"},
                new String[]{"tissue_composition", "composition"},
                new String[]{"sample_anatomic_site", "sample_anatomic_site"},
                new String[]{"sample_procurement_method", "sample_procurement_method"},
                new String[]{"platform", "platform"},
                new String[]{"files", "files"}
        };

        String defaultSort = "sample_id_num"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("program", "programs"),
                Map.entry("arm", "study_acronym"),
                Map.entry("subject_id", "subject_id_num"),
                Map.entry("sample_id", "sample_id_num"),
                Map.entry("diagnosis", "diagnoses"),
                Map.entry("tissue_type", "tissue_type"),
                Map.entry("tissue_composition", "composition"),
                Map.entry("sample_anatomic_site", "sample_anatomic_site"),
                Map.entry("sample_procurement_method", "sample_procurement_method"),
                Map.entry("platform", "platform")
        );

        params.put(SORT_DIRECTION, sortDirection);

        return overview(SAMPLES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params, String sortDirection) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        final String[][] PROPERTIES = new String[][]{
                new String[]{"program", "programs"},
                new String[]{"program_id", "program_id"},
                new String[]{"arm", "study_acronym"},
                new String[]{"subject_id", "subject_ids"},
                new String[]{"sample_id", "sample_ids"},
                new String[]{"file_id", "file_ids"},
                new String[]{"file_name", "file_names"},
                new String[]{"association", "association"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"diagnosis", "diagnoses"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("program", "programs"),
                Map.entry("arm", "study_acronym"),
                Map.entry("subject_id", "subject_id_num"),
                Map.entry("sample_id", "sample_id_num"),
                Map.entry("file_id", "file_id_num"),
                Map.entry("file_name", "file_names"),
                Map.entry("association", "association"),
                Map.entry("file_description", "file_description"),
                Map.entry("file_format", "file_format"),
                Map.entry("file_size", "file_size"),
                Map.entry("diagnosis", "diagnoses")
        );

        params.put(SORT_DIRECTION, sortDirection);

        return overview(FILES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> overview(String endpoint, Map<String, Object> params, String[][] properties, String defaultSort, Map<String, String> mapping) throws IOException {

        Request request = new Request("GET", endpoint);
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
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
