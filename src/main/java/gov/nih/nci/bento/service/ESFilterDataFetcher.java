package gov.nih.nci.bento.service;

import com.google.gson.*;
import graphql.schema.idl.RuntimeWiring;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
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
    final String SUBJECT_ID = "subject_ids";
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
                            return searchSubjects2(args);
                        })
                        .dataFetcher("searchSubjects4", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects(args);
                        })
                        // wire up "Group counts"
                        .dataFetcher("subjectCountByProgram2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByProgram(args);
                        })
                        .dataFetcher("subjectCountByStudy2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByStudy(args);
                        })
                        .dataFetcher("subjectCountByDiagnoses2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByDiagnoses(args);
                        })
                        .dataFetcher("subjectCountByRecurrenceScore2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByRecurrenceScore(args);
                        })
                        .dataFetcher("subjectCountByTumorSize2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTumorSize(args);
                        })
                        .dataFetcher("subjectCountByTumorGrade2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTumorGrade(args);
                        })
                        .dataFetcher("subjectCountByErStatus2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByErStatus(args);
                        })
                        .dataFetcher("subjectCountByPrStatus2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByPrStatus(args);
                        })
                        .dataFetcher("subjectCountByChemotherapyRegimen2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByChemotherapyRegimen(args);
                        })
                        .dataFetcher("subjectCountByEndocrineTherapy2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByEndocrineTherapy(args);
                        })
                        .dataFetcher("subjectCountByMenopauseStatus2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByMenopauseStatus(args);
                        })
                        .dataFetcher("subjectCountByTissueType2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTissueType(args);
                        })
                        .dataFetcher("subjectCountByTissueComposition2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTissueComposition(args);
                        })
                        .dataFetcher("subjectCountByFileAssociation2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByFileAssociation(args);
                        })
                        .dataFetcher("subjectCountByFileType2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByFileType(args);
                        })
                        // wire up Facet search counts
                        .dataFetcher("filterSubjectCountByProgram2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByProgram(args);
                        })
                        .dataFetcher("filterSubjectCountByStudy2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByStudy(args);
                        })
                        .dataFetcher("filterSubjectCountByDiagnoses2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByDiagnoses(args);
                        })
                        .dataFetcher("filterSubjectCountByRecurrenceScore2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByRecurrenceScore(args);
                        })
                        .dataFetcher("filterSubjectCountByTumorSize2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTumorSize(args);
                        })
                        .dataFetcher("filterSubjectCountByTumorGrade2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTumorGrade(args);
                        })
                        .dataFetcher("filterSubjectCountByErStatus2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByErStatus(args);
                        })
                        .dataFetcher("filterSubjectCountByPrStatus2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByPrStatus(args);
                        })
                        .dataFetcher("filterSubjectCountByChemotherapyRegimen2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByChemotherapyRegimen(args);
                        })
                        .dataFetcher("filterSubjectCountByEndocrineTherapy2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByEndocrineTherapy(args);
                        })
                        .dataFetcher("filterSubjectCountByMenopauseStatus2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByMenopauseStatus(args);
                        })
                        .dataFetcher("filterSubjectCountByTissueType2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTissueType(args);
                        })
                        .dataFetcher("filterSubjectCountByTissueComposition2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTissueComposition(args);
                        })
                        .dataFetcher("filterSubjectCountByFileAssociation2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByFileAssociation(args);
                        })
                        .dataFetcher("filterSubjectCountByFileType2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByFileType(args);
                        })
                )
                .build();
    }

    private List<String> searchSubjects2(Map<String, Object> params) throws IOException {
        Request request = new Request("GET", SUBJECTS_END_POINT);
        Map<String, Object> query = buildFilterQuery(params, Set.of(PAGE_SIZE));
        query.put("size", MAX_ES_SIZE);
        query.put("sort", SUBJECT_ID);
        request.setJsonEntity(gson.toJson(query));

        List<String> subject_ids = esService.collectField(request, SUBJECT_ID);

        return subject_ids;
    }

    private Map<String, Object> buildFilterQuery(Map<String, Object> params, Set<String> excludedParams) {
        Map<String, Object> result = new HashMap<>();

        List<Object> filter = new ArrayList<>();
        for (var key: params.keySet()) {
            if (excludedParams.contains(key)) {
                continue;
            }
            List<String> valueSet = (List<String>) params.get(key);
            if (valueSet.size() > 0) {
                filter.add(Map.of(
                        "terms", Map.of( key, valueSet)
                ));
            }
        }

        result.put("query", Map.of("bool", Map.of("filter", filter)));
        return result;
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException {
        // Query related values
        final String SAMPLE_ID = "sample_ids";
        final String FILE_ID = "file_ids";
        final String SAMPLES_END_POINT = "samples/_search";
        final String FILES_END_POINT = "files/_search";
        final String[] AGG_NAMES = new String[]{"programs", "studies", "lab_procedures"};
        final String[][] PROPERTIES = new String[][]{
                new String[]{"subject_id", "subject_ids"},
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
        Map<String, Object> query = buildFilterQuery(params, Set.of(PAGE_SIZE));
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

    private List<Map<String, Object>> subjectCountByProgram(Map<String, Object> params) throws IOException {
        return subjectCountBy("programs", params);

    }

    private List<Map<String, Object>> subjectCountByStudy(Map<String, Object> params) throws IOException {
        return subjectCountBy("studies", params);

    }

    private List<Map<String, Object>> subjectCountByDiagnoses(Map<String, Object> params) throws IOException {
        return subjectCountBy("diagnoses", params);

    }

    private List<Map<String, Object>> subjectCountByRecurrenceScore(Map<String, Object> params) throws IOException {
        return subjectCountBy("rc_scores", params);

    }

    private List<Map<String, Object>> subjectCountByTumorSize(Map<String, Object> params) throws IOException {
        return subjectCountBy("tumor_sizes", params);

    }

    private List<Map<String, Object>> subjectCountByTumorGrade(Map<String, Object> params) throws IOException {
        return subjectCountBy("tumor_grades", params);

    }

    private List<Map<String, Object>> subjectCountByErStatus(Map<String, Object> params) throws IOException {
        return subjectCountBy("er_status", params);

    }

    private List<Map<String, Object>> subjectCountByPrStatus(Map<String, Object> params) throws IOException {
        return subjectCountBy("pr_status", params);

    }

    private List<Map<String, Object>> subjectCountByChemotherapyRegimen(Map<String, Object> params) throws IOException {
        return subjectCountBy("chemo_regimen", params);

    }

    private List<Map<String, Object>> subjectCountByEndocrineTherapy(Map<String, Object> params) throws IOException {
        return subjectCountBy("endo_therapies", params);

    }

    private List<Map<String, Object>> subjectCountByMenopauseStatus(Map<String, Object> params) throws IOException {
        return subjectCountBy("meno_status", params);

    }

    private List<Map<String, Object>> subjectCountByTissueType(Map<String, Object> params) throws IOException {
        return subjectCountBy("tissue_type", params);

    }

    private List<Map<String, Object>> subjectCountByTissueComposition(Map<String, Object> params) throws IOException {
        return subjectCountBy("composition", params);

    }

    private List<Map<String, Object>> subjectCountByFileAssociation(Map<String, Object> params) throws IOException {
        return subjectCountBy("association", params);

    }
    private List<Map<String, Object>> subjectCountByFileType(Map<String, Object> params) throws IOException {
        return subjectCountBy("file_type", params);

    }

    private List<Map<String, Object>> filterSubjectCountByProgram(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("programs", params);

    }

    private List<Map<String, Object>> filterSubjectCountByStudy(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("studies", params);

    }

    private List<Map<String, Object>> filterSubjectCountByDiagnoses(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("diagnoses", params);

    }

    private List<Map<String, Object>> filterSubjectCountByRecurrenceScore(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("rc_scores", params);

    }

    private List<Map<String, Object>> filterSubjectCountByTumorSize(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("tumor_sizes", params);

    }

    private List<Map<String, Object>> filterSubjectCountByTumorGrade(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("tumor_grades", params);

    }

    private List<Map<String, Object>> filterSubjectCountByErStatus(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("er_status", params);

    }

    private List<Map<String, Object>> filterSubjectCountByPrStatus(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("pr_status", params);

    }

    private List<Map<String, Object>> filterSubjectCountByChemotherapyRegimen(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("chemo_regimen", params);

    }

    private List<Map<String, Object>> filterSubjectCountByEndocrineTherapy(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("endo_therapies", params);

    }

    private List<Map<String, Object>> filterSubjectCountByMenopauseStatus(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("meno_status", params);

    }

    private List<Map<String, Object>> filterSubjectCountByTissueType(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("tissue_type", params);

    }

    private List<Map<String, Object>> filterSubjectCountByTissueComposition(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("composition", params);

    }
    private List<Map<String, Object>> filterSubjectCountByFileAssociation(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("association", params);

    }
    private List<Map<String, Object>> filterSubjectCountByFileType(Map<String, Object> params) throws IOException {
        return filterSubjectCountBy("file_type", params);

    }

    private List<Map<String, Object>> subjectCountBy(String category, Map<String, Object> params) throws IOException {
        Map<String, Object> query = buildFilterQuery(params, Set.of(PAGE_SIZE));
        return getGroupCount(category, query);
    }
    private List<Map<String, Object>> filterSubjectCountBy(String category, Map<String, Object> params) throws IOException {
        Map<String, Object> query = buildFilterQuery(params, Set.of(PAGE_SIZE, category));
        return getGroupCount(category, query);
    }

    private List<Map<String, Object>> getGroupCount(String category, Map<String, Object> query) throws IOException {
        String[] AGG_NAMES = new String[] {category};
        query.put("size", 0);
        addAggregations(query, AGG_NAMES);
        Request request = new Request("GET", SUBJECTS_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        Response response = esService.send(request);
        Map<String, Object> result = collectAggs(response, AGG_NAMES);
        Map<String, JsonArray> aggs = (Map<String, JsonArray>) result.get(AGGS);
        JsonArray buckets = aggs.get(category);

        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement group: buckets) {
            data.add(Map.of("group", group.getAsJsonObject().get("key").getAsString(),
                    "subjects", group.getAsJsonObject().get("doc_count").getAsInt()
                    ));

        }

        return data;
    }

}
