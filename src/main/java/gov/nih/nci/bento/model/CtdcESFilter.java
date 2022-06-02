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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class CtdcESFilter implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(CtdcESFilter.class);

    // parameters used in queries
    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";
    final String SORT_DIRECTION = "sort_direction";

    //Cases endpoints
    final String CASES_END_POINT = "/cases/_search";
    final String CASES_COUNT_END_POINT = "/cases/_count";

    //Clinical trials endpoints
    final String CLINICAL_TRIALS_END_POINT = "/clinical_trials/_search";
    final String CLINICAL_TRIALS_COUNT_END_POINT = "/clinical_trials/_count";

    //Files endpoints
    final String FILES_END_POINT = "/files/_search";
    final String FILES_COUNT_END_POINT = "/files/_count";

    //Arms endpoints
    final String ARMS_END_POINT = "/arms/_search";
    final String ARMS_COUNT_END_POINT = "/arms/_count";

    //Nodes endpoints
    final String NODES_END_POINT = "/model_nodes/_search";
    final String NODES_COUNT_END_POINT = "/model_nodes/_count";

    //Global search constants
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
    final String GS_ABOUT_END_POINT = "/about_page/_search";
    final String GS_MODEL_END_POINT = "/data_model/_search";


    //Range constants (Not used for CTDC but required as paramaters)
    final Set<String> RANGE_PARAMS = Set.of();

    @Autowired
    ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("caseOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return caseOverview(args);
                        })
                        .dataFetcher("fileOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileOverview(args);
                        })
                        .dataFetcher("searchCases", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchCases(args);
                        })
                        .dataFetcher("globalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                        .dataFetcher("fileIdsFromFileName", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileIdsFromFileName(args);
                        })
                        .dataFetcher("fileIdsFromCaseId", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileIdsFromCaseIds(args);
                        })
                        .dataFetcher("filesInList", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filesInList(args);
                        })
                        .dataFetcher("filesOfCase", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filesOfCase(args);
                        })
                        .dataFetcher("idsLists", env -> idsLists())
                )
                .build();
    }

    private List<Map<String, Object>> caseOverview(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"case_id","case_id"},
                new String[]{"trial_code","clinical_trial_designation"},
                new String[]{"trial_id","clinical_trial_id"},
                new String[]{"arm_id","arm_id"},
                new String[]{"arm_treatment","arm_treatment"},
                new String[]{"diagnosis","disease"},
                new String[]{"gender","gender"},
                new String[]{"race","race"},
                new String[]{"ethnicity","ethnicity"},
                new String[]{"files", "uuid"}
        };

        String defaultSort = "case_id";

        Map<String, String> mapping = Map.ofEntries(
            Map.entry("case_id","case_id"),
            Map.entry("trial_code","clinical_trial_designation"),
            Map.entry("trial_id","clinical_trial_id"),
            Map.entry("arm_id","arm_id"),
            Map.entry("arm_treatment","arm_treatment"),
            Map.entry("diagnosis","disease"),
            Map.entry("gender","gender"),
            Map.entry("race","race"),
            Map.entry("ethnicity","ethnicity"),
            Map.entry("files", "uuid")
        );

        return overview(CASES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"file_name","file_name"},
                new String[]{"association","association"},
                new String[]{"description","description"},
                new String[]{"file_format","file_format"},
                new String[]{"size","size"},
                new String[]{"trial_code","trial_code"},
                new String[]{"arm","arm"},
                new String[]{"case_id","case_id"},
                new String[]{"file_id", "uuid"}
        };

        String defaultSort = "file_name";

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("file_name","file_name"),
                Map.entry("association","association"),
                Map.entry("description","description"),
                Map.entry("file_format","file_format"),
                Map.entry("size","size"),
                Map.entry("trial_code","trial_code"),
                Map.entry("arm","arm"),
                Map.entry("case_id","case_id"),
                Map.entry("file_id", "uuid")
        );

        return overview(FILES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private Map<String, Object> searchCases(Map<String, Object> params) throws IOException {
        //Get node counts
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS);
        int numberOfCases = sendCountRequest(query, CASES_COUNT_END_POINT);
        int numberOfFiles = sendCountRequest(query, FILES_COUNT_END_POINT);

        //Group count data used multiple times
        List<Map<String, Object>> trialsGroupCountData = getSubjectCount("clinical_trial_id", CASES_END_POINT, params);
        List<Map<String, Object>> diagnosisGroupCountData = getSubjectCount("disease", CASES_END_POINT, params);
        List<Map<String, Object>> fileTypeGroupCountData = getSubjectCount("file_type", FILES_END_POINT, params);
        List<Map<String, Object>> trialArmGroupCountData = getSubjectCount("trial_arm", CASES_END_POINT, params);


        Map<String, Object> data = new HashMap<>();
        data.put("numberOfTrials", trialsGroupCountData.size());
        data.put("numberOfCases", numberOfCases);
        data.put("numberOfFiles", numberOfFiles);
        data.put("numberOfArms", trialArmGroupCountData.size());
        data.put("numberOfDiagnoses", diagnosisGroupCountData.size());
        data.put("numberOfFileTypes", fileTypeGroupCountData.size());

        data.put("diagnosisCountByArm", diagnosisCountByArm(params));
        data.put("armCountByTrial", armCountByTrial(params));
        data.put("trialsAndArms", trialsAndArms(params));

        data.put("casesCountBaseOnTrialId", trialsGroupCountData);
        data.put("casesCountBaseOnTrialCode", getSubjectCount("clinical_trial_designation", CASES_END_POINT, params));
        data.put("casesCountBaseOnPubMedID", getSubjectCount("pubmed_id", CASES_END_POINT, params));
        data.put("casesCountBaseOnGender", getSubjectCount("gender", CASES_END_POINT, params));
        data.put("casesCountBaseOnRace", getSubjectCount("race", CASES_END_POINT, params));
        data.put("casesCountBaseOnEthnicity", getSubjectCount("ethnicity", CASES_END_POINT, params));
        data.put("casesCountBaseOnDiagnoses", diagnosisGroupCountData);
        data.put("casesCountBaseOnFileType", fileTypeGroupCountData);
        data.put("casesCountBaseOnFileFormat", getSubjectCount("file_format", FILES_END_POINT, params));
        data.put("casesCountBaseOnTrialArm", trialArmGroupCountData);


        data.put("filterCasesCountBaseOnTrialId", getFilterSubjectCount("clinical_trial_id", CASES_END_POINT, params));
        data.put("filterCasesCountBaseOnTrialCode", getFilterSubjectCount("clinical_trial_designation", CASES_END_POINT, params));
        data.put("filterCasesCountBaseOnPubMedID", getFilterSubjectCount("pubmed_id", CASES_END_POINT, params));
        data.put("filterCasesCountBaseOnGender", getFilterSubjectCount("gender", CASES_END_POINT, params));
        data.put("filterCasesCountBaseOnRace", getFilterSubjectCount("race", CASES_END_POINT, params));
        data.put("filterCasesCountBaseOnEthnicity", getFilterSubjectCount("ethnicity", CASES_END_POINT, params));
        data.put("filterCasesCountBaseOnDiagnoses", getFilterSubjectCount("disease", CASES_END_POINT, params));
        data.put("filterCasesCountBaseOnFileType", getFilterSubjectCount("file_type", FILES_END_POINT, params));
        data.put("filterCasesCountBaseOnFileFormat", getFilterSubjectCount("file_format", FILES_END_POINT, params));
        data.put("filterCasesCountBaseOnTrialArm", getFilterSubjectCount("trial_arm", CASES_END_POINT, params));

        return data;
    }

    private Map<String, Object> globalSearch(Map<String, Object> params) throws IOException {
        //Extract inputs from params
        String input = (String) params.get("input");
        int size = (int) params.get("first");
        int offset = (int) params.get("offset");
        //Create search categories
        List<Map<String, Object>> searchCategories = new ArrayList<>();
        //Clinical Trials Search Category Map
        searchCategories.add(Map.of(
                GS_END_POINT, CLINICAL_TRIALS_END_POINT,
                GS_COUNT_ENDPOINT, CLINICAL_TRIALS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "clinical_trial_count",
                GS_RESULT_FIELD, "clinical_trials",
                GS_SEARCH_FIELD, List.of(
                        "clinical_trial_id", "clinical_trial_long_name", "clinical_trial_short_name",
                        "clinical_trial_designation"),
                GS_SORT_FIELD, "clinical_trial_id_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"clinical_trial_id", "clinical_trial_id"},
                        new String[]{"clinical_trial_long_name", "clinical_trial_long_name"},
                        new String[]{"clinical_trial_short_name", "clinical_trial_short_name"},
                        new String[]{"clinical_trial_designation", "clinical_trial_designation"}
                },
                GS_CATEGORY_TYPE, "clinical_trial"
        ));
        //Arms Search Category Map
        searchCategories.add(Map.of(
                GS_END_POINT, ARMS_END_POINT,
                GS_COUNT_ENDPOINT, ARMS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "arm_count",
                GS_RESULT_FIELD, "arms",
                GS_SEARCH_FIELD, List.of(
                        "arm", "arm_id", "arm_drug"
                ),
                GS_SORT_FIELD, "arm_id_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"arm", "arm"},
                        new String[]{"arm_id", "arm_id"},
                        new String[]{"arm_drug", "arm_drug"}
                },
                GS_CATEGORY_TYPE, "arm"
        ));
        //Cases Search Category Map
        searchCategories.add(Map.of(
                GS_END_POINT, CASES_END_POINT,
                GS_COUNT_ENDPOINT, CASES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "case_count",
                GS_RESULT_FIELD, "cases",
                GS_SEARCH_FIELD, List.of(
                        "case_id_gs", "clinical_trial_id_gs", "clinical_trial_code_gs", "arm_id_gs", "gender_gs", "race_gs", "disease_gs"
                ),
                GS_SORT_FIELD, "case_id",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"case_id", "case_id_gs"},
                        new String[]{"clinical_trial_id", "clinical_trial_id_gs"},
                        new String[]{"clinical_trial_code", "clinical_trial_code_gs"},
                        new String[]{"arm_id", "arm_id_gs"},
                        new String[]{"gender", "gender_gs"},
                        new String[]{"race", "race_gs"},
                        new String[]{"disease", "disease_gs"}
                },
                GS_CATEGORY_TYPE, "case"
        ));
        //Files Search Category Map
        searchCategories.add(Map.of(
                GS_END_POINT, FILES_END_POINT,
                GS_COUNT_ENDPOINT, FILES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "file_count",
                GS_RESULT_FIELD, "files",
                GS_SEARCH_FIELD, List.of(
                        "file_name_gs", "file_description_gs", "file_type_gs", "file_format_gs", "file_size_gs",
                        "clinical_trials_code_gs", "clinical_trial_id_gs"
                ),
                GS_SORT_FIELD, "file_name",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"file_name", "file_name_gs"},
                        new String[]{"file_description", "file_description_gs"},
                        new String[]{"file_type", "file_type_gs"},
                        new String[]{"file_format", "file_format_gs"},
                        new String[]{"file_size", "file_size_gs"},
                        new String[]{"clinical_trial_id", "clinical_trial_id_gs"},
                        new String[]{"clinical_trial_code", "clinical_trial_code_gs"}
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
                GS_HIGHLIGHT_FIELDS, new String[][]{
                        new String[]{"highlight", "node"}
                },
                GS_CATEGORY_TYPE, "node"
        ));

        Set<String> combinedCategories = Set.of("model");

        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> category : searchCategories) {
            String countResultFieldName = (String) category.get(GS_COUNT_RESULT_FIELD);
            String resultFieldName = (String) category.get(GS_RESULT_FIELD);
            String[][] properties = (String[][]) category.get(GS_COLLECT_FIELDS);
            String[][] highlights = (String[][]) category.get(GS_HIGHLIGHT_FIELDS);
            Map<String, Object> query = getGlobalSearchQuery(input, category);

            // Get count
            Request countRequest = new Request("GET", (String) category.get(GS_COUNT_ENDPOINT));
            countRequest.setJsonEntity(gson.toJson(query));
            JsonObject countResult = esService.send(countRequest);
            int oldCount = (int) result.getOrDefault(countResultFieldName, 0);
            result.put(countResultFieldName, countResult.get("count").getAsInt() + oldCount);

            // Get results
            Request request = new Request("GET", (String) category.get(GS_END_POINT));
            String sortFieldName = (String) category.get(GS_SORT_FIELD);
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
            List<Map<String, Object>> objects = esService.collectPage(jsonObject, properties, highlights, (int) query.get("size"), 0);

            for (var object : objects) {
                object.put(GS_CATEGORY_TYPE, category.get(GS_CATEGORY_TYPE));
            }

            List<Map<String, Object>> existingObjects = (List<Map<String, Object>>) result.getOrDefault(resultFieldName, null);
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

        for (String category : combinedCategories) {
            List<Object> pagedCategory = paginate((List) result.get(category), size, offset);
            result.put(category, pagedCategory);
        }

        return result;
    }

    private List<Map<String, Object>> fileIdsFromFileName(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"file_name", "file_name"},
                new String[]{"uuid", "uuid"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("file_name", "file_name"),
                Map.entry("uuid", "uuid")
        );

        return listQuery(params, PROPERTIES, mapping, defaultSort, FILES_END_POINT);
    }

    private List<Map<String, Object>> filesInList(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"clinical_trial_code", "trial_code"},
                new String[]{"case_id", "case_id"},
                new String[]{"arm_id", "arm_id"},
                new String[]{"file_type", "file_type"},
                new String[]{"association", "association"},
                new String[]{"file_description", "description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "size"},
                new String[]{"uuid", "uuid"},
                new String[]{"md5sum", "md5sum"},
                new String[]{"file_name", "file_name"}
        };


        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("clinical_trial_code", "trial_code"),
                Map.entry("case_id", "case_id"),
                Map.entry("arm_id", "arm_id"),
                Map.entry("file_type", "file_type"),
                Map.entry("association", "association"),
                Map.entry("file_description", "description"),
                Map.entry("file_format", "file_format"),
                Map.entry("file_size", "size"),
                Map.entry("uuid", "uuid"),
                Map.entry("md5sum", "md5sum"),
                Map.entry("file_name", "file_name")
        );

        return listQuery(params, PROPERTIES, mapping, defaultSort, FILES_END_POINT);
    }

    private List<Map<String, Object>> filesOfCase(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"parent", "association"},
                new String[]{"file_name", "file_name"},
                new String[]{"file_type", "file_type"},
                new String[]{"file_description", "description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "size"},
                new String[]{"md5sum", "md5sum"},
                new String[]{"uuid", "uuid"},
                new String[]{"association", "association"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("parent", "association"),
                Map.entry("file_name", "file_name"),
                Map.entry("file_type", "file_type"),
                Map.entry("file_description", "description"),
                Map.entry("file_format", "file_format"),
                Map.entry("file_size", "size"),
                Map.entry("md5sum", "md5sum"),
                Map.entry("uuid", "uuid"),
                Map.entry("association", "association")
        );

        return listQuery(params, PROPERTIES, mapping, defaultSort, FILES_END_POINT);
    }

    private Map<String, Object> idsLists() throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"case_id", "case_id"}
        };
        Request request = new Request("GET", CASES_END_POINT);
        List<Map<String, Object>> response = esService.collectPage(request, new HashMap<String, Object>(), PROPERTIES, ESService.MAX_ES_SIZE, 0);
        ArrayList<String> case_ids = new ArrayList<>();
        response.forEach(x -> case_ids.add((String) x.get("case_id")));
        Map<String, Object> result = Map.of(
                "case_ids", case_ids
        );
        return result;
    }

    private Map<String, Object> fileIdsFromCaseIds(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"file_id", "uuid"}
        };
        Map<String, Object> query = esService.buildListQuery(params, Set.of());
        Request request = new Request("GET", FILES_END_POINT);
        List<Map<String, Object>> response = esService.collectPage(request, query, PROPERTIES, ESService.MAX_ES_SIZE, 0);
        ArrayList<String> file_ids = new ArrayList<>();
        response.forEach(x -> file_ids.add((String) x.get("file_id")));
        Map<String, Object> result = Map.of(
                "file_ids", file_ids
        );
        return result;
    }


    private List<Map<String, Object>> listQuery(
            Map<String, Object> params,
            String[][] properties,
            Map<String, String> mapping,
            String defaultSort,
            String endpoint
    ) throws IOException {
        Map<String, Object> query = esService.buildListQuery(params, Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
        String order_by = (String) params.get(ORDER_BY);
        String direction = ((String) params.get(SORT_DIRECTION)).toLowerCase();
        query.put("sort", mapSortOrder(order_by, direction, defaultSort, mapping));
        int pageSize = (int) params.get(PAGE_SIZE);
        int offset = (int) params.get(OFFSET);
        Request request = new Request("GET", endpoint);
        return esService.collectPage(request, query, properties, pageSize, offset);
    }

    private List paginate(List org, int pageSize, int offset) {
        List<Object> result = new ArrayList<>();
        int size = org.size();
        if (offset <= size - 1) {
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

        for (JsonElement hit : jsonObject.get("hits").getAsJsonObject().get("hits").getAsJsonArray()) {
            for (JsonElement highlight : hit.getAsJsonObject().get("highlight").getAsJsonObject().get(ABOUT_CONTENT).getAsJsonArray()) {
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

    private Map<String, Object> getGlobalSearchQuery(String input, Map<String, Object> category) {
        List<String> searchFields = (List<String>) category.get(GS_SEARCH_FIELD);
        List<Object> searchClauses = new ArrayList<>();
        for (String searchFieldName : searchFields) {
            searchClauses.add(Map.of("match_phrase_prefix", Map.of(searchFieldName, input)));
        }
        Map<String, Object> query = new HashMap<>();
        query.put("query", Map.of("bool", Map.of("should", searchClauses)));
        return query;
    }

    private Map<String, Object> addHighlight(Map<String, Object> query, Map<String, Object> category) {
        Map<String, Object> result = new HashMap<>(query);
        List<String> searchFields = (List<String>) category.get(GS_SEARCH_FIELD);
        Map<String, Object> highlightClauses = new HashMap<>();
        for (String searchFieldName : searchFields) {
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

    private int sendCountRequest(Map<String, Object> query, String endpoint) throws IOException {
        Request countRequest = new Request("GET", endpoint);
        countRequest.setJsonEntity(gson.toJson(query));
        JsonObject countResult = esService.send(countRequest);
        return countResult.get("count").getAsInt();
    }

    private List<Map<String, Object>> diagnosisCountByArm(Map<String, Object> params) throws IOException {
        final String category = "arm_id";
        final String subCategory = "disease";
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE));
        String[] AGG_NAMES = new String[]{category};
        query = esService.addAggregations(query, AGG_NAMES);
        String[] subCategories = new String[]{subCategory};
        esService.addSubAggregations(query, category, subCategories);
        Request request = new Request("GET", CASES_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, AGG_NAMES);
        JsonArray arms = aggs.get(category);

        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement arm : arms) {
            String armId = arm.getAsJsonObject().get("key").getAsString();
            int diagnosesCount =
                    arm.getAsJsonObject().getAsJsonObject(subCategory).getAsJsonArray("buckets").size();
            data.add(Map.of(
                    "arm_id", armId,
                    "diagnoses", diagnosesCount
            ));
        }
        return data;
    }

    private List<Map<String, Object>> armCountByTrial(Map<String, Object> params) throws IOException {
        final String category = "clinical_trial_id";
        final String subCategory = "trial_arm";
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE));
        String[] AGG_NAMES = new String[]{category};
        query = esService.addAggregations(query, AGG_NAMES);
        String[] subCategories = new String[]{subCategory};
        esService.addSubAggregations(query, category, subCategories);
        Request request = new Request("GET", CASES_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, AGG_NAMES);
        JsonArray arms = aggs.get(category);

        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement arm : arms) {
            String group = arm.getAsJsonObject().get("key").getAsString();
            int subjects =
                    arm.getAsJsonObject().getAsJsonObject(subCategory).getAsJsonArray("buckets").size();
            data.add(Map.of(
                    "group", group,
                    "subjects", subjects
            ));
        }
        return data;
    }

    private List<Map<String, Object>> trialsAndArms(Map<String, Object> params) throws IOException {
        final String category = "clinical_trial_id";
        final String subCategory = "arm_id";

        String[] subCategories = new String[]{subCategory};
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE));
        String[] AGG_NAMES = new String[]{category};
        query = esService.addAggregations(query, AGG_NAMES);
        esService.addSubAggregations(query, category, subCategories);
        Request request = new Request("GET", CASES_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, AGG_NAMES);
        JsonArray buckets = aggs.get(category);

        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement group : buckets) {
            List<Map<String, Object>> arms = new ArrayList<>();

            for (JsonElement studyElement : group.getAsJsonObject().get(subCategory).getAsJsonObject().get("buckets").getAsJsonArray()) {
                JsonObject study = studyElement.getAsJsonObject();
                int size = study.get("doc_count").getAsInt();
                arms.add(Map.of(
                        "arm", study.get("key").getAsString(),
                        "caseSize", size
                ));
            }
            data.add(Map.of("trials", group.getAsJsonObject().get("key").getAsString(),
                    "caseSize", group.getAsJsonObject().get("doc_count").getAsInt(),
                    "arms", arms
            ));

        }
        return data;
    }

    private List<Map<String, Object>> getSubjectCount(
            String category,
            String endpoint,
            Map<String, Object> params
    ) throws IOException {
        return getGroupCount(category, endpoint, params, Set.of(PAGE_SIZE));
    }

    private List<Map<String, Object>> getFilterSubjectCount(
            String category,
            String endpoint,
            Map<String, Object> params
    ) throws IOException {
        return getGroupCount(category, endpoint, params, Set.of(PAGE_SIZE, category));
    }

    private List<Map<String, Object>> getGroupCount(
            String category,
            String endpoint,
            Map<String, Object> params,
            Set<String> excludeParams
    ) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, excludeParams);
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
