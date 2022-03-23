package gov.nih.nci.bento.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class GmbEsFilter implements DataFetcher{
    private static final Logger logger = LogManager.getLogger(GmbEsFilter.class);

    // parameters used in queries
    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";
    final String SORT_DIRECTION = "sort_direction";

    final String SUBJECTS_END_POINT = "/subjects/_search";
    final String SUBJECTS_COUNT_END_POINT = "/subjects/_count";

    final String FILES_END_POINT = "/files/_search";
    final String FILES_COUNT_END_POINT = "/files/_count";

    final String TRIALS_END_POINT = "/trials/_search";
    final String TRIALS_COUNT_END_POINT = "/trials/_count";

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
                        .dataFetcher("searchSubjects", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects(args);
                        })
                        .dataFetcher("subjectOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectOverview(args);
                        })
                        .dataFetcher("fileOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileOverview(args);
                        })
                        .dataFetcher("globalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                )
                .build();
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException {
        //Get node counts
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS);
        int numberOfSubjects = sendCountRequest(query, SUBJECTS_COUNT_END_POINT);
        int numberOfFiles = sendCountRequest(query, FILES_COUNT_END_POINT);

        Map<String, Object> data = new HashMap<>();
        data.put("numberOfTrials", getGroupCount("clinical_trial_id", SUBJECTS_END_POINT, params).size());
        data.put("numberOfSubjects", numberOfSubjects);
        data.put("numberOfFiles", numberOfFiles);

        data.put("subjectCountByRace", getGroupCount("race", SUBJECTS_END_POINT, params));
        data.put("subjectCountByDiseaseTerm", getGroupCount("disease_term", SUBJECTS_END_POINT, params));
        data.put("subjectCountByRegisteringInstitution", getGroupCount("registering_institution", SUBJECTS_END_POINT, params));
        data.put("subjectCountByPatientSubgroup", getGroupCount("patient_subgroup", SUBJECTS_END_POINT, params));
        data.put("subjectCountByStageAtEntry", getGroupCount("stage_at_entry", SUBJECTS_END_POINT, params));
        data.put("subjectCountByCauseOfDeath", getGroupCount("cause_of_death", SUBJECTS_END_POINT, params));
        data.put("subjectCountBySitesOfDiseaseAtAutopsy", getGroupCount("sites_of_disease_at_autopsy", SUBJECTS_END_POINT, params));
        data.put("subjectCountBySourceOfTheLabData", getGroupCount("source_of_the_lab_data", SUBJECTS_END_POINT, params));
        data.put("subjectCountByLabTest", getGroupCount("lab_test", SUBJECTS_END_POINT, params));
        data.put("subjectCountBySystemOrganClass", getGroupCount("system_organ_class", SUBJECTS_END_POINT, params));
        data.put("subjectCountBySerious", getGroupCount("serious", SUBJECTS_END_POINT, params));
        data.put("subjectCountByOutcome", getGroupCount("outcome", SUBJECTS_END_POINT, params));
        data.put("subjectCountBySomaticPathogenicity", getGroupCount("somatic_pathogenicity", SUBJECTS_END_POINT, params));
        data.put("subjectCountByGermlinePathogenicity", getGroupCount("germline_pathogenicity", SUBJECTS_END_POINT, params));
        data.put("subjectCountByFileType", getGroupCount("file_type", FILES_END_POINT, params));

        data.put("filterSubjectCountByRace", getGroupCount("race", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByDiseaseTerm", getGroupCount("disease_term", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByRegisteringInstitution", getGroupCount("registering_institution", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByPatientSubgroup", getGroupCount("patient_subgroup", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByStageAtEntry", getGroupCount("stage_at_entry", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByCauseOfDeath", getGroupCount("cause_of_death", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountBySitesOfDiseaseAtAutopsy", getGroupCount("sites_of_disease_at_autopsy", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountBySourceOfTheLabData", getGroupCount("source_of_the_lab_data", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByLabTest", getGroupCount("lab_test", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountBySystemOrganClass", getGroupCount("system_organ_class", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountBySerious", getGroupCount("serious", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByOutcome", getGroupCount("outcome", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountBySomaticPathogenicity", getGroupCount("somatic_pathogenicity", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByGermlinePathogenicity", getGroupCount("germline_pathogenicity", SUBJECTS_END_POINT, params));
        data.put("filterSubjectCountByFileType", getGroupCount("file_type", FILES_END_POINT, params));

        return data;
    }

    private List<Map<String, Object>> subjectOverview(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"subject_id", "subject_id"},
                new String[]{"race", "race"},
                new String[]{"disease_term", "disease_term"},
                new String[]{"stageAtEntry", "stage_at_entry"},
                new String[]{"causeOfDeath", "cause_of_death"},
                new String[]{"sitesOfDiseaseAtAutopsy", "sites_of_disease_at_autopsy"},
                new String[]{"sourceOfTheLabData", "source_of_the_lab_data"},
                new String[]{"labTest", "lab_test"},
                new String[]{"systemOrganClass", "system_organ_class"},
                new String[]{"serious", "serious"},
                new String[]{"outcome", "outcome"}
        };

        String defaultSort = "subject_id"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("subject_id", "subject_id"),
                Map.entry("race", "race"),
                Map.entry("disease_term", "disease_term"),
                Map.entry("stageAtEntry", "stage_at_entry"),
                Map.entry("causeOfDeath", "cause_of_death"),
                Map.entry("sitesOfDiseaseAtAutopsy", "sites_of_disease_at_autopsy"),
                Map.entry("sourceOfTheLabData", "source_of_the_lab_data"),
                Map.entry("labTest", "lab_test"),
                Map.entry("systemOrganClass", "system_organ_class"),
                Map.entry("serious", "serious"),
                Map.entry("outcome", "outcome")
        );

        return overview(SUBJECTS_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"file_name", "file_name"},
                new String[]{"subject_id", "subject_id"},
                new String[]{"description", "file_description"},
                new String[]{"fileFormat", "file_format"},
                new String[]{"size", "file_size"},
                new String[]{"fileType", "file_type"}
        };

        String defaultSort = "subject_id"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("file_name", "file_name"),
                Map.entry("subject_id", "subject_id"),
                Map.entry("description", "file_description"),
                Map.entry("fileFormat", "file_format"),
                Map.entry("size", "file_size"),
                Map.entry("fileType", "file_type")
        );

        return overview(FILES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private Map<String, Object> globalSearch(Map<String, Object> params) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String input = (String) params.get("input");
        int size = (int) params.get("first");
        int offset = (int) params.get("offset");
        List<Map<String, Object>> searchCategories = new ArrayList<>();
        searchCategories.add(Map.of(
                GS_END_POINT, TRIALS_END_POINT,
                GS_COUNT_ENDPOINT, TRIALS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "trial_count",
                GS_RESULT_FIELD, "trials",
                GS_SEARCH_FIELD, List.of(
                        "trial_name_gs", "trial_long_name_gs", "trial_id_gs", "trial_description_gs", "trial_type_gs"),
                GS_SORT_FIELD, "trial_id",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"clinical_trial_short_name", "trial_name_gs"},
                        new String[]{"clinical_trial_long_name", "trial_long_name_gs"},
                        new String[]{"clinical_trial_id", "trial_id_gs"},
                        new String[]{"clinical_trial_description", "trial_description_gs"},
                        new String[]{"clinical_trial_type", "trial_type_gs"}
                },
                GS_CATEGORY_TYPE, "trial"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, SUBJECTS_END_POINT,
                GS_COUNT_ENDPOINT, SUBJECTS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "subject_count",
                GS_RESULT_FIELD, "subjects",
                GS_SEARCH_FIELD, List.of("subject_id_gs"),
                GS_SORT_FIELD, "subject_id",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"subject_id", "subject_id_gs"}
                },
                GS_CATEGORY_TYPE, "subject"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, FILES_END_POINT,
                GS_COUNT_ENDPOINT, FILES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "file_count",
                GS_RESULT_FIELD, "files",
                GS_SEARCH_FIELD, List.of("file_id_gs", "file_name_gs", "file_description_gs"),
                GS_SORT_FIELD, "file_id",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"file_id", "file_id_gs"},
                        new String[]{"file_name", "file_name_gs"},
                        new String[]{"file_description", "file_description_gs"}
                },
                GS_CATEGORY_TYPE, "file"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, NODES_END_POINT,
                GS_COUNT_ENDPOINT, NODES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "model_count",
                GS_RESULT_FIELD, "model",
                GS_SEARCH_FIELD, List.of("node"),
                GS_SORT_FIELD, "node_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"node_name", "node"}
                },
                GS_HIGHLIGHT_FIELDS, new String[][] {
                        new String[]{"highlight", "node"}
                },
                GS_CATEGORY_TYPE, "node"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, PROPERTIES_END_POINT,
                GS_COUNT_ENDPOINT, PROPERTIES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "model_count",
                GS_RESULT_FIELD, "model",
                GS_SEARCH_FIELD, List.of("property", "property_description", "property_type", "property_required"),
                GS_SORT_FIELD, "property_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"node_name", "node"},
                        new String[]{"property_name", "property"},
                        new String[]{"property_type", "property_type"},
                        new String[]{"property_required", "property_required"},
                        new String[]{"property_description", "property_description"}
                },
                GS_HIGHLIGHT_FIELDS, new String[][] {
                        new String[]{"highlight", "property"},
                        new String[]{"highlight", "property_description"},
                        new String[]{"highlight", "property_type"},
                        new String[]{"highlight", "property_required"}
                },
                GS_CATEGORY_TYPE, "property"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, VALUES_END_POINT,
                GS_COUNT_ENDPOINT, VALUES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "model_count",
                GS_RESULT_FIELD, "model",
                GS_SEARCH_FIELD, List.of("value"),
                GS_SORT_FIELD, "value_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"node_name", "node"},
                        new String[]{"property_name", "property"},
                        new String[]{"property_type", "property_type"},
                        new String[]{"property_required", "property_required"},
                        new String[]{"property_description", "property_description"},
                        new String[]{"value", "value"}
                },
                GS_HIGHLIGHT_FIELDS, new String[][] {
                        new String[]{"highlight", "value"}
                },
                GS_CATEGORY_TYPE, "value"
        ));

        Set<String> combinedCategories = Set.of("model") ;

        for (Map<String, Object> category: searchCategories) {
            String countResultFieldName = (String) category.get(GS_COUNT_RESULT_FIELD);
            String resultFieldName = (String) category.get(GS_RESULT_FIELD);
            String[][] properties = (String[][]) category.get(GS_COLLECT_FIELDS);
            String[][] highlights = (String[][]) category.get(GS_HIGHLIGHT_FIELDS);
            Map<String, Object> query = getGlobalSearchQuery(input, category);

            // Get count
            Request countRequest = new Request("GET", (String) category.get(GS_COUNT_ENDPOINT));
            countRequest.setJsonEntity(gson.toJson(query));
            JsonObject countResult = esService.send(countRequest);
            int oldCount = (int)result.getOrDefault(countResultFieldName, 0);
            result.put(countResultFieldName, countResult.get("count").getAsInt() + oldCount);

            // Get results
            Request request = new Request("GET", (String)category.get(GS_END_POINT));
            String sortFieldName = (String)category.get(GS_SORT_FIELD);
            query.put("sort", Map.of(sortFieldName, "asc"));
            query = addHighlight(query, category);

            if (combinedCategories.contains(resultFieldName)) {
                query.put("size", ESService.MAX_ES_SIZE);
                query.put("from", 0);
            } else {
                query.put("size", size);
                query.put("from", offset);
            }
            request.setJsonEntity(gson.toJson(query));
            JsonObject jsonObject = esService.send(request);
            List<Map<String, Object>> objects = esService.collectPage(jsonObject, properties, highlights, (int)query.get("size"), 0);

            for (var object: objects) {
                object.put(GS_CATEGORY_TYPE, category.get(GS_CATEGORY_TYPE));
            }

            List<Map<String, Object>> existingObjects = (List<Map<String, Object>>)result.getOrDefault(resultFieldName, null);
            if (existingObjects != null) {
                existingObjects.addAll(objects);
                result.put(resultFieldName, existingObjects);
            } else {
                result.put(resultFieldName, objects);
            }

        }

        List<Map<String, String>> about_results = searchAboutPage(input);
        int about_count = about_results.size();
        result.put("about_count", about_count);
        result.put("about_page", paginate(about_results, size, offset));

        for (String category: combinedCategories) {
            List<Object> pagedCategory = paginate((List)result.get(category), size, offset);
            result.put(category, pagedCategory);
        }

        return result;
    }

    private List paginate(List org, int pageSize, int offset) {
        List<Object> result = new ArrayList<>();
        int size = org.size();
        if (offset <= size -1) {
            int end_index = offset + pageSize;
            if (end_index > size) {
                end_index = size;
            }
            result = org.subList(offset, end_index);
        }
        return result;
    }

    private List<Map<String, String>> searchAboutPage(String input) throws IOException {
        final String ABOUT_CONTENT = "content.paragraph";
        Map<String, Object> query = Map.of(
                "query", Map.of("match", Map.of(ABOUT_CONTENT, input)),
                "highlight", Map.of(
                        "fields", Map.of(ABOUT_CONTENT, Map.of()),
                        "pre_tags", GS_HIGHLIGHT_DELIMITER,
                        "post_tags", GS_HIGHLIGHT_DELIMITER
                ),
                "size", ESService.MAX_ES_SIZE
        );
        Request request = new Request("GET", GS_ABOUT_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);

        List<Map<String, String>> result = new ArrayList<>();

        for (JsonElement hit: jsonObject.get("hits").getAsJsonObject().get("hits").getAsJsonArray()) {
            for (JsonElement highlight: hit.getAsJsonObject().get("highlight").getAsJsonObject().get(ABOUT_CONTENT).getAsJsonArray()) {
                String page = hit.getAsJsonObject().get("_source").getAsJsonObject().get("page").getAsString();
                String title = hit.getAsJsonObject().get("_source").getAsJsonObject().get("title").getAsString();
                result.add(Map.of(
                        GS_CATEGORY_TYPE, GS_ABOUT,
                        "page", page,
                        "title", title,
                        "text", highlight.getAsString()
                ));
            }
        }

        return result;
    }

    private Map<String, Object> addHighlight(Map<String, Object> query, Map<String, Object> category) {
        Map<String, Object> result = new HashMap<>(query);
        List<String> searchFields = (List<String>)category.get(GS_SEARCH_FIELD);
        Map<String, Object> highlightClauses = new HashMap<>();
        for (String searchFieldName: searchFields) {
            highlightClauses.put(searchFieldName, Map.of());
        }

        result.put("highlight", Map.of(
                        "fields", highlightClauses,
                        "pre_tags", "",
                        "post_tags", "",
                        "fragment_size", 1
                )
        );
        return result;
    }

    private Map<String, Object> getGlobalSearchQuery(String input, Map<String, Object> category) {
        List<String> searchFields = (List<String>)category.get(GS_SEARCH_FIELD);
        List<Object> searchClauses = new ArrayList<>();
        for (String searchFieldName: searchFields) {
            searchClauses.add(Map.of("match_phrase_prefix", Map.of(searchFieldName, input)));
        }
        Map<String, Object> query = new HashMap<>();
        query.put("query", Map.of("bool", Map.of("should", searchClauses)));
        return query;
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



    private List<Map<String, Object>> getGroupCount(String category, String endpoint, Map<String, Object> params)
            throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE, category));
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        Request request = new Request("GET", endpoint);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, AGG_NAMES);
        //Extract group counts from result
        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement group: aggs.get(category)) {
            Map groupCount = Map.of(
                    "group", group.getAsJsonObject().get("key").getAsString(),
                    "subjects", group.getAsJsonObject().get("doc_count").getAsInt()
            );
            data.add(groupCount);
        }
        return data;
    }

    private int sendCountRequest(Map<String, Object> query, String endpoint) throws IOException {
        Request countRequest = new Request("GET", endpoint);
        countRequest.setJsonEntity(gson.toJson(query));
        JsonObject countResult = esService.send(countRequest);
        return countResult.get("count").getAsInt();
    }


}
