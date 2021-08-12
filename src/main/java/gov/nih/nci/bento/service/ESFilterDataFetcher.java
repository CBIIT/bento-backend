package gov.nih.nci.bento.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Service
public class ESFilterDataFetcher {
    private static final Logger logger = LogManager.getLogger(ESFilterDataFetcher.class);

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
        final String ID_FIELD = "subject_id";
        final String DASHBOARD_END_POINT = "subjects/_search";
        Request request = new Request("GET", DASHBOARD_END_POINT);
        Map<String, Object> query = buildQuery(params);
        query.put("size", 10000);
        query.put("sort", ID_FIELD);
        request.setJsonEntity(gson.toJson(query));

        List<String> subject_ids = esService.collectField(request, ID_FIELD);

        return subject_ids;
    }

    private Map<String, Object> buildQuery(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> query = new HashMap<String, Object>();
        result.put("query", query);
        Map<String, Object> bool = new HashMap<>();
        query.put("bool", bool);
        List<Object> filter = new ArrayList<>();
        bool.put("filter", filter);

        for (var key: params.keySet()) {
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
        final String SUBJECT_ID = "subject_id";
        final String SAMPLE_ID = "sample_id";
        final String FILE_ID = "file_id";
        final String SUBJECTS_END_POINT = "subjects/_search";
        final String SAMPLES_END_POINT = "samples/_search";
        final String FILES_END_POINT = "files/_search";
        Request request = new Request("GET", FILES_END_POINT);
        Map<String, Object> query = buildQuery(params);
        query.put("size", 10000);
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
        addAggregations(query);
        request = new Request("GET", SUBJECTS_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        Map<String, JsonArray> aggs = esService.collectAggs(request, new String[]{"programs", "studies", "lab_procedures"});

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

        return data;
    }

    private void addAggregations(Map<String, Object> query) {
        query.put("size", 0);
        Map<String, Object> aggs = Map.of(
                "programs", Map.of("terms", Map.of("field", "programs")),
                "studies", Map.of("terms", Map.of("field", "studies")),
                "lab_procedures", Map.of("terms", Map.of("field", "lab_procedures"))
        );
        query.put("aggregations", aggs);
    }
}
