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
    final String CASE_FILES_END_POINT = "/case_files/_search";
    final String CASE_FILES_COUNT_END_POINT = "/case_files/_count";
    final String STUDY_FILES_END_POINT = "/study_files/_search";
    final String STUDY_FILES_COUNT_END_POINT = "/study_files/_count";
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
    final Set<String> RANGE_PARAMS = Set.of();


    @Autowired
    ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchCases", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchCases(args);
                        })
                        .dataFetcher("caseOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return caseOverview(args);
                        })
                        .dataFetcher("sampleOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return sampleOverview(args);
                        })
                        .dataFetcher("caseFileOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return caseFileOverview(args);
                        })
                        .dataFetcher("studyFileOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return studyFileOverview(args);
                        })
                )
                .build();
    }

    private Map<String, Object> searchCases(Map<String, Object> params) throws IOException {
        final String AGG_NAME = "agg_name";
        final String AGG_ENDPOINT = "agg_endpoint";
        final String WIDGET_QUERY = "widgetQueryName";
        final String FILTER_COUNT_QUERY = "filterCountQueryName";

        // Query related values
        final List<Map<String, String>> TERM_AGGS = new ArrayList<>();
        TERM_AGGS.add(Map.of(
                AGG_NAME, "program",
                WIDGET_QUERY, "caseCountByProgram",
                FILTER_COUNT_QUERY, "filterCaseCountByProgram",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "study",
                WIDGET_QUERY, "caseCountByStudyCode",
                FILTER_COUNT_QUERY, "filterCaseCountByStudyCode",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "study_type",
                WIDGET_QUERY, "caseCountByStudyType",
                FILTER_COUNT_QUERY, "filterCaseCountByStudyType",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "biobank",
                WIDGET_QUERY, "caseCountByBiobank",
                FILTER_COUNT_QUERY, "filterCaseCountByBiobank",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "study_participation",
                WIDGET_QUERY, "caseCountByStudyParticipation",
                FILTER_COUNT_QUERY, "filterCaseCountByStudyParticipation",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "breed",
                WIDGET_QUERY, "caseCountByBreed",
                FILTER_COUNT_QUERY, "filterCaseCountByBreed",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "diagnosis",
                WIDGET_QUERY, "caseCountByDiagnosis",
                FILTER_COUNT_QUERY, "filterCaseCountByDiagnosis",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "disease_site",
                WIDGET_QUERY, "caseCountByDiseaseSite",
                FILTER_COUNT_QUERY, "filterCaseCountByDiseaseSite",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "stage_of_disease",
                WIDGET_QUERY,"caseCountByStageOfDisease",
                FILTER_COUNT_QUERY, "filterCaseCountByStageOfDisease",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "response_to_treatment",
                WIDGET_QUERY,"caseCountByResponseToTreatment",
                FILTER_COUNT_QUERY, "filterCaseCountByResponseToTreatment",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "sex",
                WIDGET_QUERY, "caseCountByGender",
                FILTER_COUNT_QUERY, "filterCaseCountBySex",
                AGG_ENDPOINT, CASES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "neutered_status",
                WIDGET_QUERY, "caseCountByNeuteredStatus",
                FILTER_COUNT_QUERY, "filterCaseCountByNeuteredStatus",
                AGG_ENDPOINT, CASES_END_POINT
        ));

        TERM_AGGS.add(Map.of(
                AGG_NAME, "sample_site",
                WIDGET_QUERY, "caseCountBySampleSite",
                FILTER_COUNT_QUERY, "filterCaseCountBySampleSite",
                AGG_ENDPOINT, SAMPLES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "sample_type",
                WIDGET_QUERY, "caseCountBySampleType",
                FILTER_COUNT_QUERY, "filterCaseCountBySampleType",
                AGG_ENDPOINT, SAMPLES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "sample_pathology",
                WIDGET_QUERY, "caseCountBySamplePathology",
                FILTER_COUNT_QUERY, "filterCaseCountBySamplePathology",
                AGG_ENDPOINT, SAMPLES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "file_association",
                WIDGET_QUERY, "caseCountByFileAssociation",
                FILTER_COUNT_QUERY, "filterCaseCountByFileAssociation",
                AGG_ENDPOINT, CASE_FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "file_type",
                WIDGET_QUERY, "caseCountByFileType",
                FILTER_COUNT_QUERY, "filterCaseCountByFileType",
                AGG_ENDPOINT, CASE_FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "file_format",
                WIDGET_QUERY, "caseCountByFileFormat",
                FILTER_COUNT_QUERY, "filterCaseCountByFileFormat",
                AGG_ENDPOINT, CASE_FILES_END_POINT
        ));

        List<String> agg_names = new ArrayList<>();
        for (var agg: TERM_AGGS) {
            agg_names.add(agg.get(AGG_NAME));
        }
        final String[] TERM_AGG_NAMES = agg_names.toArray(new String[TERM_AGGS.size()]);

        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(), Set.of("first"));
        Request sampleCountRequest = new Request("GET", SAMPLES_COUNT_END_POINT);
        sampleCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject sampleCountResult = esService.send(sampleCountRequest);
        int numberOfSamples = sampleCountResult.get("count").getAsInt();

        Request caseFileCountRequest = new Request("GET", CASE_FILES_COUNT_END_POINT);
        caseFileCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject fileCountResult = esService.send(caseFileCountRequest);
        int numberOfCaseFiles = fileCountResult.get("count").getAsInt();

        Request studyFileCountRequest = new Request("GET", STUDY_FILES_COUNT_END_POINT);
        studyFileCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject studyFileCountResult = esService.send(studyFileCountRequest);
        int numberOfStudyFiles = studyFileCountResult.get("count").getAsInt();

        Request caseCountRequest = new Request("GET", CASES_COUNT_END_POINT);
        caseCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject caseCountResult = esService.send(caseCountRequest);
        int numberOfCases = caseCountResult.get("count").getAsInt();


        // Get aggregations
        Map<String, Object> aggQuery = esService.addAggregations(query, TERM_AGG_NAMES);
        Request caseRequest = new Request("GET", CASES_END_POINT);
        caseRequest.setJsonEntity(gson.toJson(aggQuery));
        JsonObject caseResult = esService.send(caseRequest);
        Map<String, JsonArray> aggs = esService.collectTermAggs(caseResult, TERM_AGG_NAMES);

        Map<String, Object> data = new HashMap<>();
        data.put("numberOfPrograms", aggs.get("program").size());
        data.put("numberOfStudies", aggs.get("study").size());
        data.put("numberOfCases", numberOfCases);
        data.put("numberOfSamples", numberOfSamples);
        data.put("numberOfFiles", numberOfCaseFiles + numberOfStudyFiles);
        data.put("numberOfStudyFiles", numberOfStudyFiles);
        data.put("numberOfAliquots", 0);
        data.put("volumeOfData", getVolumeOfData(params, "file_size"));


        data.put("programsAndStudies", programsAndStudies(params));

        // widgets data and facet filter counts
        for (var agg: TERM_AGGS) {
            String field = agg.get(AGG_NAME);
            String widgetQueryName = agg.get(WIDGET_QUERY);
            String filterCountQueryName = agg.get(FILTER_COUNT_QUERY);
            String endpoint = agg.get(AGG_ENDPOINT);
            // subjectCountByXXXX
            List<Map<String, Object>> widgetData;
            if (endpoint.equals(CASES_END_POINT)) {
                widgetData = getGroupCountHelper(aggs.get(field));
                data.put(widgetQueryName, widgetData);
            } else {
                widgetData = subjectCountBy(field, params, endpoint);;
                data.put(widgetQueryName, widgetData);
            }
            // filterSubjectCountByXXXX
            if (params.containsKey(field) && ((List<String>)params.get(field)).size() > 0) {
                List<Map<String, Object>> filterCount = filterSubjectCountBy(field, params, endpoint);;
                data.put(filterCountQueryName, filterCount);
            } else {
                data.put(filterCountQueryName, widgetData);
            }
        }

        return data;
    }

    private double getVolumeOfData(Map<String, Object> params, String fieldName) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params);
        query = esService.addSumAggregation(query, fieldName);
        Request request = new Request("GET", CASE_FILES_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        return esService.retrieveSumAgg(jsonObject, fieldName);
    }

    private List<Map<String, Object>> programsAndStudies(Map<String, Object> params) throws IOException {
        final String category = "program";
        final String subCategory = "study_code";

        String[] subCategories = new String[] { subCategory };
        Map<String, Object> query = esService.buildFacetFilterQuery(params);
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        esService.addSubAggregations(query, category, subCategories);
        Request request = new Request("GET", CASES_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, AGG_NAMES);
        JsonArray buckets = aggs.get(category);

        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement group: buckets) {
            List<Map<String, Object>> studies = new ArrayList<>();

            for (JsonElement studyElement: group.getAsJsonObject().get(subCategory).getAsJsonObject().get("buckets").getAsJsonArray()) {
                JsonObject study = studyElement.getAsJsonObject();
                int size = study.get("doc_count").getAsInt();
                studies.add(Map.of(
                        "study", study.get("key").getAsString(),
                        "caseSize", size
                ));
            }
            data.add(Map.of("program", group.getAsJsonObject().get("key").getAsString(),
                    "caseSize", group.getAsJsonObject().get("doc_count").getAsInt(),
                    "studies", studies
            ));
        }
        return data;
    }

    private List<Map<String, Object>> subjectCountBy(String category, Map<String, Object> params, String endpoint) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE));
        return getGroupCount(category, query, endpoint);
    }

    private List<Map<String, Object>> filterSubjectCountBy(String category, Map<String, Object> params, String endpoint) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE, category));
        return getGroupCount(category, query, endpoint);
    }

    private List<Map<String, Object>> getGroupCount(String category, Map<String, Object> query, String endpoint) throws IOException {
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        Request request = new Request("GET", endpoint);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, AGG_NAMES);
        JsonArray buckets = aggs.get(category);

        return getGroupCountHelper(buckets);
    }

    private List<Map<String, Object>> getGroupCountHelper(JsonArray buckets) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement group: buckets) {
            data.add(Map.of("group", group.getAsJsonObject().get("key").getAsString(),
                    "count", group.getAsJsonObject().get("doc_count").getAsInt()
            ));

        }
        return data;
    }

    private List<Map<String, Object>> caseOverview(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"case_id", "case_ids"},
                new String[]{"study_code", "study_code"},
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
                Map.entry("study_code", "study_code"),
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

        return overview(CASES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> sampleOverview(Map<String, Object> params) throws IOException {
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

        return overview(SAMPLES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> caseFileOverview(Map<String, Object> params) throws IOException {
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
                Map.entry("file_uuid", "file_uuids"),
                Map.entry("access_file", "file_size")
        );

        return overview(CASE_FILES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }


    private List<Map<String, Object>> studyFileOverview(Map<String, Object> params) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        final String[][] PROPERTIES = new String[][]{
                new String[]{"file_name", "file_name"},
                new String[]{"file_type", "file_type"},
                new String[]{"association", "file_association"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"study_code", "study_code"},
                new String[]{"file_uuid", "file_uuids"},
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("file_name", "file_name"),
                Map.entry("file_type", "file_type"),
                Map.entry("association", "file_association"),
                Map.entry("file_description", "file_description"),
                Map.entry("file_format", "file_format"),
                Map.entry("file_size", "file_size"),
                Map.entry("study_code", "study_code"),
                Map.entry("file_uuid", "file_uuids"),
                Map.entry("access_file", "file_size")
        );

        return overview(STUDY_FILES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }


    private List<Map<String, Object>> overview(String endpoint, Map<String, Object> params, String[][] properties, String defaultSort, Map<String, String> mapping) throws IOException {

        Request request = new Request("GET", endpoint);
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(), Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
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
