package gov.nih.nci.bento.model;

import com.google.gson.*;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Service
public class ESFilterDataFetcher {
    private static final Logger logger = LogManager.getLogger(ESFilterDataFetcher.class);

    // parameters used in queries
    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";
    final String SORT_DIRECTION = "sort_direction";
    final String SUBJECT_ID = "subject_ids";
    final String SUBJECT_ID_NUM = "subject_id_num";
    final String SUBJECTS_END_POINT = "/subjects/_search";
    final String SUBJECTS_COUNT_END_POINT = "/subjects/_count";
    final String SAMPLE_ID = "sample_ids";
    final String SAMPLE_ID_NUM = "sample_id_num";
    final String SAMPLES_END_POINT = "/samples/_search";
    final String SAMPLES_COUNT_END_POINT = "/samples/_count";
    final String FILE_ID = "file_ids";
    final String FILE_ID_NUM = "file_id_num";
    final String FILES_END_POINT = "/files/_search";
    final String FILES_COUNT_END_POINT = "/files/_count";
    final String GS_END_POINT = "/global_search/_search";
    final int GS_LIMIT = 10;
    final String GS_RESULT_FIELD = "result_field";
    final String GS_SEARCH_FIELD = "search_field";
    final String GS_COLLECT_FIELD = "collect_field";
    final String GS_AGG_LIST = "list";
    final Set<String> RANGE_PARAMS = Set.of("age_at_index");


    @Autowired
    ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchSubjects", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects(args);
                        })
                        .dataFetcher("subjectOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectOverview(args);
                        })
                        .dataFetcher("sampleOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return sampleOverview(args);
                        })
                        .dataFetcher("fileOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileOverview(args);
                        })
                        .dataFetcher("globalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                        .dataFetcher("fileIDsFromList", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileIDsFromList(args);
                        })
                )
                .build();
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException {
        // Query related values
        final Map<String, List<String>> TERM_AGGS = new HashMap<>();
        TERM_AGGS.put("programs", List.of("subjectCountByProgram", "filterSubjectCountByProgram"));
        TERM_AGGS.put("studies", List.of("subjectCountByStudy", "filterSubjectCountByStudy"));
        TERM_AGGS.put("diagnoses", List.of("subjectCountByDiagnoses", "filterSubjectCountByDiagnoses"));
        TERM_AGGS.put("rc_scores", List.of("subjectCountByRecurrenceScore", "filterSubjectCountByRecurrenceScore"));
        TERM_AGGS.put("tumor_sizes", List.of("subjectCountByTumorSize", "filterSubjectCountByTumorSize"));
        TERM_AGGS.put("tumor_grades", List.of("subjectCountByTumorGrade", "filterSubjectCountByTumorGrade"));
        TERM_AGGS.put("er_status", List.of("subjectCountByErStatus", "filterSubjectCountByErStatus"));
        TERM_AGGS.put("pr_status", List.of("subjectCountByPrStatus", "filterSubjectCountByPrStatus"));
        TERM_AGGS.put("chemo_regimen", List.of("subjectCountByChemotherapyRegimen", "filterSubjectCountByChemotherapyRegimen"));
        TERM_AGGS.put("endo_therapies", List.of("subjectCountByEndocrineTherapy", "filterSubjectCountByEndocrineTherapy"));
        TERM_AGGS.put("meno_status", List.of("subjectCountByMenopauseStatus", "filterSubjectCountByMenopauseStatus"));
        TERM_AGGS.put("tissue_type", List.of("subjectCountByTissueType", "filterSubjectCountByTissueType"));
        TERM_AGGS.put("composition", List.of("subjectCountByTissueComposition", "filterSubjectCountByTissueComposition"));
        TERM_AGGS.put("association", List.of("subjectCountByFileAssociation", "filterSubjectCountByFileAssociation"));
        TERM_AGGS.put("file_type", List.of("subjectCountByFileType", "filterSubjectCountByFileType"));
        TERM_AGGS.put("lab_procedures", List.of("subjectCountByLabProcedures", "filterSubjectCountByLabProcedures"));

        final String[] TERM_AGG_NAMES = TERM_AGGS.keySet().toArray(new String[0]);

        final Map<String, String> RANGE_AGGS = new HashMap<>();
        RANGE_AGGS.put("age_at_index",  "filterSubjectCountByAge");
        final String[] RANGE_AGG_NAMES = RANGE_AGGS.keySet().toArray(new String[0]);

        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS);
        Request sampleCountRequest = new Request("GET", SAMPLES_COUNT_END_POINT);
        sampleCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject sampleCountResult = esService.send(sampleCountRequest);
        int numberOfSamples = sampleCountResult.get("count").getAsInt();

        Request fileCountRequest = new Request("GET", FILES_COUNT_END_POINT);
        fileCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject fileCountResult = esService.send(fileCountRequest);
        int numberOfFiles = fileCountResult.get("count").getAsInt();

        Request subjectCountRequest = new Request("GET", SUBJECTS_COUNT_END_POINT);
        subjectCountRequest.setJsonEntity(gson.toJson(query));
        JsonObject subjectCountResult = esService.send(subjectCountRequest);
        int numberOfSubjects = subjectCountResult.get("count").getAsInt();


        // Get aggregations
        Map<String, Object> aggQuery = esService.addAggregations(query, TERM_AGG_NAMES, RANGE_AGG_NAMES);
        Request subjectRequest = new Request("GET", SUBJECTS_END_POINT);
        subjectRequest.setJsonEntity(gson.toJson(aggQuery));
        JsonObject subjectResult = esService.send(subjectRequest);
        Map<String, JsonArray> aggs = esService.collectTermAggs(subjectResult, TERM_AGG_NAMES);

        Map<String, Object> data = new HashMap<>();
        data.put("numberOfPrograms", aggs.get("programs").size());
        data.put("numberOfStudies", aggs.get("studies").size());
        data.put("numberOfLabProcedures", aggs.get("lab_procedures").size());
        data.put("numberOfSubjects", numberOfSubjects);
        data.put("numberOfSamples", numberOfSamples);
        data.put("numberOfFiles", numberOfFiles);

        data.put("armsByPrograms", armsByPrograms(params));
        // widgets data and facet filter counts
        for (String field: TERM_AGG_NAMES) {
            String widgetQueryName = TERM_AGGS.get(field).get(0);
            String filterCountQueryName = TERM_AGGS.get(field).get(1);
            List<Map<String, Object>> widgetData = getGroupCountHelper(aggs.get(field));
            data.put(widgetQueryName, widgetData);
            if (params.containsKey(field) && ((List<String>)params.get(field)).size() > 0) {
                List<Map<String, Object>> filterCount = filterSubjectCountBy(field, params);;
                data.put(filterCountQueryName, filterCount);
            } else {
                data.put(filterCountQueryName, widgetData);
            }
        }

        Map<String, JsonObject> rangeAggs = esService.collectRangeAggs(subjectResult, RANGE_AGG_NAMES);

        for (String field: RANGE_AGG_NAMES) {
            String filterCountQueryName = RANGE_AGGS.get(field);
            if (params.containsKey(field) && ((List<Double>)params.get(field)).size() >= 2) {
                Map<String, Object> filterCount = rangeFilterSubjectCountBy(field, params);;
                data.put(filterCountQueryName, filterCount);
            } else {
                data.put(filterCountQueryName, getRange(rangeAggs.get(field)));
            }
        }

        return data;
    }

    private List<Map<String, Object>> subjectOverview(Map<String, Object> params) throws IOException {
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

        return overview(SUBJECTS_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> sampleOverview(Map<String, Object> params) throws IOException {
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

        return overview(SAMPLES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params) throws IOException {
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

    private List<Map<String, Object>> armsByPrograms(Map<String, Object> params) throws IOException {
        final String category = "programs";
        final String subCategory = "study_acronym";

        String[] subCategories = new String[] { subCategory };
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE));
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        esService.addSubAggregations(query, category, subCategories);
        Request request = new Request("GET", SUBJECTS_END_POINT);
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
                        "arm", study.get("key").getAsString(),
                        "caseSize", size,
                        "size", size
                ));
            }
            data.add(Map.of("program", group.getAsJsonObject().get("key").getAsString(),
                    "caseSize", group.getAsJsonObject().get("doc_count").getAsInt(),
                    "children", studies
            ));

        }
        return data;
    }

    private List<Map<String, Object>> filterSubjectCountBy(String category, Map<String, Object> params) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS,Set.of(PAGE_SIZE, category));
        return getGroupCount(category, query);
    }

    private List<Map<String, Object>> getGroupCount(String category, Map<String, Object> query) throws IOException {
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        Request request = new Request("GET", SUBJECTS_END_POINT);
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
                    "subjects", group.getAsJsonObject().get("doc_count").getAsInt()
            ));

        }
        return data;
    }

    private Map<String, Object> rangeFilterSubjectCountBy(String category, Map<String, Object> params) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS,Set.of(PAGE_SIZE, category));
        return getRangeCount(category, query);
    }

    private Map<String, Object> getRangeCount(String category, Map<String, Object> query) throws IOException {
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, new String[]{}, AGG_NAMES);
        Request request = new Request("GET", SUBJECTS_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonObject> aggs = esService.collectRangeAggs(jsonObject, AGG_NAMES);
        return getRange(aggs.get(category).getAsJsonObject());
    }

    private Map<String, Object> getRange(JsonObject aggs) {
        Map<String, Object> range = new HashMap<>();
        range.put("lowerBound", aggs.get("min").getAsDouble());
        range.put("upperBound", aggs.get("max").getAsDouble());
        range.put("subjects", aggs.get("count").getAsInt());
        return range;
    }

    private Map<String, Object> globalSearch(Map<String, Object> params) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String input = (String) params.get("input");
        List<Map<String, String>> fieldNames = new ArrayList<>();
        fieldNames.add(Map.of(
                GS_RESULT_FIELD, "program_ids",
                GS_SEARCH_FIELD,"program_id",
                GS_COLLECT_FIELD,"program_id_kw"

        ));
        fieldNames.add(Map.of(
                GS_RESULT_FIELD, "arm_ids",
                GS_SEARCH_FIELD,"arm_id",
                GS_COLLECT_FIELD,"arm_id_kw"

        ));
        fieldNames.add(Map.of(
                GS_RESULT_FIELD, "subject_ids",
                GS_SEARCH_FIELD,"subject_id",
                GS_COLLECT_FIELD,"subject_id_kw"

        ));
        fieldNames.add(Map.of(
                GS_RESULT_FIELD, "sample_ids",
                GS_SEARCH_FIELD,"sample_id",
                GS_COLLECT_FIELD,"sample_id_kw"

        ));
        fieldNames.add(Map.of(
                GS_RESULT_FIELD, "file_ids",
                GS_SEARCH_FIELD,"file_id",
                GS_COLLECT_FIELD,"file_id_kw"

        ));
        Map<String, Map<String, Object>> queries = getGlobalSearchQuery(input, fieldNames);

        for (String resultFieldName: queries.keySet()) {
            Map<String, Object> query = queries.get(resultFieldName);
            Request request = new Request("GET", GS_END_POINT);
            request.setJsonEntity(gson.toJson(query));
            JsonObject jsonObject = esService.send(request);
            Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, new String[]{GS_AGG_LIST});
            var buckets = aggs.get(GS_AGG_LIST);
            result.put(resultFieldName, esService.collectBucketKeys(buckets));
        }

        return result;
    }

    private Map<String, Map<String, Object>> getGlobalSearchQuery(String input, List<Map<String, String>> fieldNames) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (var field: fieldNames) {
            String searchFieldName = field.get(GS_SEARCH_FIELD);
            String collectFieldName = field.get(GS_COLLECT_FIELD);
            String resultFieldName = field.get(GS_RESULT_FIELD);
            result.put(
                    resultFieldName,
                    Map.of(
                        "query", Map.of("match_phrase_prefix", Map.of(searchFieldName, input)),
                        "_source", false,
                        "aggs", Map.of(GS_AGG_LIST, Map.of("terms", Map.of("field", collectFieldName))),
                         "size", GS_LIMIT
                    )
            );
        }

        return result;
    }

    private List<String> fileIDsFromList(Map<String, Object> params) throws IOException {
        String idField = "file_ids";
        String[] idFieldArray = new String[]{idField};
        Map<String, Object> query = esService.buildListQuery(params, Set.of());
        query = esService.addAggregations(query, idFieldArray);
        Request request = new Request("GET", FILES_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        List<String> result = esService.collectTerms(jsonObject, idField);
        return result;
    }
}
