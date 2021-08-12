package gov.nih.nci.bento.service;

import com.google.gson.*;
import graphql.schema.idl.RuntimeWiring;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.json.JSONArray;
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
    final String SUBJECT_ID = "subject_id";
    final String SUBJECTS_END_POINT = "subjects/_search";
    final int MAX_ES_SIZE = 10000;
    final String JSON_OBJECT = "jsonObject";
    final String AGGS = "aggs";

    @Autowired ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    public RuntimeWiring buildRuntimeWiring(){
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchSubjects3", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects3(args);
                        })
                        .dataFetcher("searchSubjects4", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects4(args);
                        })
                )
                .build();
    }

    private List<String> searchSubjects3(Map<String, Object> params) throws IOException {
        Request request = new Request("GET", SUBJECTS_END_POINT);
        Map<String, Object> query = buildQuery(params, Set.of(PAGE_SIZE));
        query.put("size", 10000);
        query.put("sort", SUBJECT_ID);
        request.setJsonEntity(gson.toJson(query));

        List<String> subject_ids = esService.collectField(request, SUBJECT_ID);

        return subject_ids;
    }

    private Map<String, Object> buildQuery(Map<String, Object> params, Set<String> excludedParams) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> query = new HashMap<String, Object>();
        result.put("query", query);
        Map<String, Object> bool = new HashMap<>();
        query.put("bool", bool);
        List<Object> filter = new ArrayList<>();
        bool.put("filter", filter);

        for (var key: params.keySet()) {
            if (excludedParams.contains(key)) {
                continue;
            }
            List<String> valueSet = (List<String>)params.get(key);
            if (valueSet.size() > 0) {
                Map<String, Object> terms = new HashMap<>();
                filter.add(terms);
                Map<String, List<String>> field = new HashMap<>();
                terms.put("terms", field);
                field.put(key, valueSet);
            }
        }

        return result;
    }

    private Map<String, Object> searchSubjects4(Map<String, Object> params) throws IOException {
        // Query related values
        final String SAMPLE_ID = "sample_id";
        final String FILE_ID = "file_id";
        final String SAMPLES_END_POINT = "samples/_search";
        final String FILES_END_POINT = "files/_search";
        final String[] AGG_NAMES = new String[]{"programs", "studies", "lab_procedures"};
        final String[][] PROPERTIES = new String[][]{
                new String[]{"subject_id", "subject_id"},
                new String[]{"program", "programs"},
                new String[]{"program_id", "program_id"},
                new String[]{"study_acronym", "study_acronym"},
                new String[]{"diagnosis", "diagnoses"},
                new String[]{"recurrence_score", "rc_scores"},
                new String[]{"tumor_size", "tumor_sizes"},
                new String[]{"tumor_grade", "tumor_grades"},
                new String[]{"er_status", "er_status"},
                new String[]{"pr_status", "pr_status"},
                new String[]{"age_at_index", "age_at_index"},
                new String[]{"survival_time", "survival_time"},
                new String[]{"survival_time_unit", "survival_time_unit"}
        };

        Request request = new Request("GET", FILES_END_POINT);
        Map<String, Object> query = buildQuery(params, Set.of(PAGE_SIZE));
        query.put("size", MAX_ES_SIZE);
        query.put("sort", FILE_ID);
        request.setJsonEntity(gson.toJson(query));

        // Collect file_ids
        List<String> file_ids = esService.collectField(request, FILE_ID);

        // Reuse query to collect sample_ids
        request = new Request("GET", SAMPLES_END_POINT);
        query.put("sort", SAMPLE_ID);
        request.setJsonEntity(gson.toJson(query));
        List<String> sample_ids = esService.collectField(request, SAMPLE_ID);

        // Again, collect subject_ids
        request = new Request("GET", SUBJECTS_END_POINT);
        query.put("sort", SUBJECT_ID);
        request.setJsonEntity(gson.toJson(query));
        List<String> subject_ids = esService.collectField(request, SUBJECT_ID);

        // Get aggregations
        addAggregations(query, AGG_NAMES);
        int pageSize = (int)params.get(PAGE_SIZE);
        query.put("size", pageSize);
        request = new Request("GET", SUBJECTS_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        Response response = esService.send(request);
        Map<String, Object> result = collectAggs(response, AGG_NAMES);
        Map<String, JsonArray> aggs = (Map<String, JsonArray>) result.get(AGGS);
        JsonObject jsonObject = (JsonObject) result.get(JSON_OBJECT);

        List<Map<String, Object>> firstPage = collectFirstPage(jsonObject, PROPERTIES, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("numberOfPrograms", aggs.get("programs").size());
        data.put("numberOfStudies", aggs.get("studies").size());
        data.put("numberOfSubjects", subject_ids.size());
        data.put("numberOfSamples", sample_ids.size());
        data.put("numberOfLabProcedures", aggs.get("lab_procedures").size());
        data.put("numberOfFiles", file_ids.size());
        data.put("subjectIds", subject_ids);
        data.put("sampleIds", sample_ids);
        data.put("fileIds", file_ids);
        data.put("firstPage", firstPage);

        return data;
    }

    private void addAggregations(Map<String, Object> query, String[] aggNames) {
        query.put("size", 0);
        Map<String, Object> aggs = new HashMap<>();
        for (String aggName: aggNames) {
            aggs.put(aggName, Map.of("terms", Map.of("field", aggName)));
        }
        query.put("aggregations", aggs);
    }

    private Map<String, Object> collectAggs(Response response, String[] aggNames) throws IOException{
        Map<String, Object> result = new HashMap<>();
        Map<String, JsonArray> data = new HashMap<>();
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        result.put(JSON_OBJECT, jsonObject);
        JsonObject aggs = jsonObject.getAsJsonObject("aggregations");
        for (String aggName: aggNames) {
            data.put(aggName, aggs.getAsJsonObject(aggName).getAsJsonArray("buckets"));
        }
        result.put(AGGS, data);
        return result;
    }

    private List<Map<String, Object>> collectFirstPage(JsonObject jsonObject, String[][] properties, int pageSize) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();

        JsonArray searchHits = jsonObject.getAsJsonObject("hits").getAsJsonArray("hits");
        int size = Math.min(searchHits.size(), pageSize);
        for (int i = 0; i < size; i++) {
            Map<String, Object> row = new HashMap<>();
            for (String[] prop: properties) {
                String propName = prop[0];
                String dataField = prop[1];
                JsonElement element = searchHits.get(i).getAsJsonObject().get("_source").getAsJsonObject().get(dataField);
                if (element != null) {
                    row.put(propName, element.getAsString());
                } else {
                    row.put(propName, null);
                }
            }
            data.add(row);
        }

        return data;
    }
}
