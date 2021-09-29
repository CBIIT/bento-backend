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
    final String SAMPLE_ID = "sample_ids";
    final String SAMPLE_ID_NUM = "sample_id_num";
    final String SAMPLES_END_POINT = "/samples/_search";
    final String FILE_ID = "file_ids";
    final String FILE_ID_NUM = "file_id_num";
    final String FILES_END_POINT = "/files/_search";
    final String GS_END_POINT = "/global_search/_search";
    final int GS_LIMIT = 10;
    final String GS_RESULT_FIELD = "result_field";
    final String GS_SEARCH_FIELD = "search_field";
    final String GS_COLLECT_FIELD = "collect_field";
    final String GS_AGG_LIST = "list";


    @Autowired
    ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchSubjects2", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects2(args);
                        })
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
                        // wire up "Group counts"
                        .dataFetcher("armsByPrograms", env -> {
                            Map<String, Object> args = env.getArguments();
                            return armsByPrograms(args);
                        })
                        .dataFetcher("subjectCountByProgram", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByProgram(args);
                        })
                        .dataFetcher("subjectCountByStudy", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByStudy(args);
                        })
                        .dataFetcher("subjectCountByDiagnoses", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByDiagnoses(args);
                        })
                        .dataFetcher("subjectCountByRecurrenceScore", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByRecurrenceScore(args);
                        })
                        .dataFetcher("subjectCountByTumorSize", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTumorSize(args);
                        })
                        .dataFetcher("subjectCountByTumorGrade", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTumorGrade(args);
                        })
                        .dataFetcher("subjectCountByErStatus", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByErStatus(args);
                        })
                        .dataFetcher("subjectCountByPrStatus", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByPrStatus(args);
                        })
                        .dataFetcher("subjectCountByChemotherapyRegimen", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByChemotherapyRegimen(args);
                        })
                        .dataFetcher("subjectCountByEndocrineTherapy", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByEndocrineTherapy(args);
                        })
                        .dataFetcher("subjectCountByMenopauseStatus", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByMenopauseStatus(args);
                        })
                        .dataFetcher("subjectCountByTissueType", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTissueType(args);
                        })
                        .dataFetcher("subjectCountByTissueComposition", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByTissueComposition(args);
                        })
                        .dataFetcher("subjectCountByFileAssociation", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByFileAssociation(args);
                        })
                        .dataFetcher("subjectCountByFileType", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectCountByFileType(args);
                        })
                        // wire up Facet search counts
                        .dataFetcher("filterSubjectCountByProgram", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByProgram(args);
                        })
                        .dataFetcher("filterSubjectCountByStudy", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByStudy(args);
                        })
                        .dataFetcher("filterSubjectCountByDiagnoses", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByDiagnoses(args);
                        })
                        .dataFetcher("filterSubjectCountByRecurrenceScore", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByRecurrenceScore(args);
                        })
                        .dataFetcher("filterSubjectCountByTumorSize", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTumorSize(args);
                        })
                        .dataFetcher("filterSubjectCountByTumorGrade", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTumorGrade(args);
                        })
                        .dataFetcher("filterSubjectCountByErStatus", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByErStatus(args);
                        })
                        .dataFetcher("filterSubjectCountByPrStatus", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByPrStatus(args);
                        })
                        .dataFetcher("filterSubjectCountByChemotherapyRegimen", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByChemotherapyRegimen(args);
                        })
                        .dataFetcher("filterSubjectCountByEndocrineTherapy", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByEndocrineTherapy(args);
                        })
                        .dataFetcher("filterSubjectCountByMenopauseStatus", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByMenopauseStatus(args);
                        })
                        .dataFetcher("filterSubjectCountByTissueType", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTissueType(args);
                        })
                        .dataFetcher("filterSubjectCountByTissueComposition", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByTissueComposition(args);
                        })
                        .dataFetcher("filterSubjectCountByFileAssociation", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByFileAssociation(args);
                        })
                        .dataFetcher("filterSubjectCountByFileType", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filterSubjectCountByFileType(args);
                        })
                        .dataFetcher("globalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                )
                .build();
    }

    private List<String> searchSubjects2(Map<String, Object> params) throws IOException {
        Request request = new Request("GET", SUBJECTS_END_POINT);
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(PAGE_SIZE));
        query.put("size", esService.MAX_ES_SIZE);
        query.put("sort", SUBJECT_ID_NUM);
        request.setJsonEntity(gson.toJson(query));

        List<String> subject_ids = esService.collectField(request, SUBJECT_ID);

        return subject_ids;
    }


    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException {
        // Query related values
        final Map<String, List<String>> AGGS = new HashMap<>();
        AGGS.put("programs", List.of("subjectCountByProgram", "filterSubjectCountByProgram"));
        AGGS.put("studies", List.of("subjectCountByStudy", "filterSubjectCountByStudy"));
        AGGS.put("diagnoses", List.of("subjectCountByDiagnoses", "filterSubjectCountByDiagnoses"));
        AGGS.put("rc_scores", List.of("subjectCountByRecurrenceScore", "filterSubjectCountByRecurrenceScore"));
        AGGS.put("tumor_sizes", List.of("subjectCountByTumorSize", "filterSubjectCountByTumorSize"));
        AGGS.put("tumor_grades", List.of("subjectCountByTumorGrade", "filterSubjectCountByTumorGrade"));
        AGGS.put("er_status", List.of("subjectCountByErStatus", "filterSubjectCountByErStatus"));
        AGGS.put("pr_status", List.of("subjectCountByPrStatus", "filterSubjectCountByPrStatus"));
        AGGS.put("chemo_regimen", List.of("subjectCountByChemotherapyRegimen", "filterSubjectCountByChemotherapyRegimen"));
        AGGS.put("endo_therapies", List.of("subjectCountByEndocrineTherapy", "filterSubjectCountByEndocrineTherapy"));
        AGGS.put("meno_status", List.of("subjectCountByMenopauseStatus", "filterSubjectCountByMenopauseStatus"));
        AGGS.put("tissue_type", List.of("subjectCountByTissueType", "filterSubjectCountByTissueType"));
        AGGS.put("composition", List.of("subjectCountByTissueComposition", "filterSubjectCountByTissueComposition"));
        AGGS.put("association", List.of("subjectCountByFileAssociation", "filterSubjectCountByFileAssociation"));
        AGGS.put("file_type", List.of("subjectCountByFileType", "filterSubjectCountByFileType"));
        AGGS.put("lab_procedures", List.of("subjectCountByLabProcedures", "filterSubjectCountByLabProcedures"));

        final String[] AGG_NAMES = AGGS.keySet().toArray(new String[0]);

        Map<String, Object> query = esService.buildFacetFilterQuery(params);
        query.put("size", 0);
        Request sampleRequest = new Request("GET", SAMPLES_END_POINT);
        sampleRequest.setJsonEntity(gson.toJson(query));
        JsonObject sampleResult = esService.send(sampleRequest);
        int numberOfSamples = esService.getTotalHits(sampleResult);

        Request fileRequest = new Request("GET", FILES_END_POINT);
        fileRequest.setJsonEntity(gson.toJson(query));
        JsonObject fileResult = esService.send(fileRequest);
        int numberOfFiles = esService.getTotalHits(fileResult);


        // Get aggregations
        Map<String, Object> aggQuery = esService.addAggregations(query, AGG_NAMES);
        Request subjectRequest = new Request("GET", SUBJECTS_END_POINT);
        subjectRequest.setJsonEntity(gson.toJson(aggQuery));
        JsonObject subjectResult = esService.send(subjectRequest);
        int numberOfSubjects = esService.getTotalHits(subjectResult);
        Map<String, JsonArray> aggs = esService.collectAggs(subjectResult, AGG_NAMES);

        Map<String, Object> data = new HashMap<>();
        data.put("numberOfPrograms", aggs.get("programs").size());
        data.put("numberOfStudies", aggs.get("studies").size());
        data.put("numberOfLabProcedures", aggs.get("lab_procedures").size());
        data.put("numberOfSubjects", numberOfSubjects);
        data.put("numberOfSamples", numberOfSamples);
        data.put("numberOfFiles", numberOfFiles);

        data.put("armsByPrograms", armsByPrograms(params));
        for (String field: AGG_NAMES) {
            String widgetQueryName = AGGS.get(field).get(0);
            String filterCountQueryName = AGGS.get(field).get(1);
            List<Map<String, Object>> widgetData = getGroupCountHelper(aggs.get(field));
            data.put(widgetQueryName, widgetData);
            if (params.containsKey(field) && ((List<String>)params.get(field)).size() > 0) {
                List<Map<String, Object>> filterCount = filterSubjectCountBy(field, params);;
                data.put(filterCountQueryName, filterCount);
            } else {
                data.put(filterCountQueryName, widgetData);
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
                new String[]{"sample_id", "sample_id"},
                new String[]{"file_id", "file_ids"},
                new String[]{"file_name", "file_name"},
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
                Map.entry("file_name", "file_name"),
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
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
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
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(PAGE_SIZE));
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        esService.addSubAggregations(query, category, subCategories);
        Request request = new Request("GET", SUBJECTS_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectAggs(jsonObject, AGG_NAMES);
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
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(PAGE_SIZE));
        return getGroupCount(category, query);
    }
    private List<Map<String, Object>> filterSubjectCountBy(String category, Map<String, Object> params) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(PAGE_SIZE, category));
        return getGroupCount(category, query);
    }

    private List<Map<String, Object>> getGroupCount(String category, Map<String, Object> query) throws IOException {
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        Request request = new Request("GET", SUBJECTS_END_POINT);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectAggs(jsonObject, AGG_NAMES);
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
            Map<String, JsonArray> aggs = esService.collectAggs(jsonObject, new String[]{GS_AGG_LIST});
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
}
