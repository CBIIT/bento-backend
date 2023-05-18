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

public class CdsEsFilter implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(CdsEsFilter.class);

    // parameters used in queries
    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";
    final String SORT_DIRECTION = "sort_direction";

    final String STUDIES_END_POINT = "/studies/_search";
    final String STUDIES_COUNT_END_POINT = "/studies/_count";

    final String SUBJECTS_END_POINT = "/subjects/_search";
    final String SUBJECTS_COUNT_END_POINT = "/subjects/_count";
    final String SUBJECT_IDS_END_POINT = "/subject_ids/_search";
    final String SAMPLES_END_POINT = "/samples/_search";
    final String SAMPLES_COUNT_END_POINT = "/samples/_count";
    final String FILES_END_POINT = "/files/_search";
    final String FILES_EXPERIMENTAL_STRATEGY_END_POINT = "/file_experimental_strategies/_search";
    final String FILES_COUNT_END_POINT = "/file_ids/_count";
    final String PROGRAMS_END_POINT = "/programs/_search";
    final String PROGRAMS_COUNT_END_POINT = "/programs/_count";
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
    final Set<String> RANGE_PARAMS = Set.of("number_of_study_participants", "number_of_study_samples");

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
                        .dataFetcher("findSubjectIdsInList", env -> {
                            Map<String, Object> args = env.getArguments();
                            return findSubjectIdsInList(args);
                        })
                )
                .build();
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException {
        final String AGG_NAME = "agg_name";
        final String AGG_ENDPOINT = "agg_endpoint";
        final String WIDGET_QUERY = "widgetQueryName";
        final String FILTER_COUNT_QUERY = "filterCountQueryName";
        // Query related values
        final List<Map<String, String>> TERM_AGGS = new ArrayList<>();
        TERM_AGGS.add(Map.of(
                AGG_NAME, "studies",
                WIDGET_QUERY, "subjectCountByStudy",
                FILTER_COUNT_QUERY, "filterSubjectCountByStudy",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "experimental_strategies",
                WIDGET_QUERY, "subjectCountByExperimentalStrategy",
                FILTER_COUNT_QUERY, "filterSubjectCountByExperimentalStrategy",
                AGG_ENDPOINT, FILES_EXPERIMENTAL_STRATEGY_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "accesses",
                WIDGET_QUERY, "subjectCountByAccess",
                FILTER_COUNT_QUERY, "filterSubjectCountByAccess",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "genders",
                WIDGET_QUERY, "subjectCountByGender",
                FILTER_COUNT_QUERY, "filterSubjectCountByGender",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "is_tumor",
                WIDGET_QUERY,"subjectCountByIsTumor",
                FILTER_COUNT_QUERY, "filterSubjectCountByIsTumor",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "analyte_type",
                WIDGET_QUERY, "subjectCountByAnalyteType",
                FILTER_COUNT_QUERY, "filterSubjectCountByAnalyteType",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "file_types",
                WIDGET_QUERY, "subjectCountByFileType",
                FILTER_COUNT_QUERY, "filterSubjectCountByFileType",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "site",
                WIDGET_QUERY, "subjectCountByDiseaseSite",
                FILTER_COUNT_QUERY, "filterSubjectCountByDiseaseSite",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "library_strategies",
                WIDGET_QUERY, "subjectCountByLibraryStrategy",
                FILTER_COUNT_QUERY, "filterSubjectCountByLibraryStrategy",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "library_sources",
                WIDGET_QUERY, "subjectCountByLibrarySource",
                FILTER_COUNT_QUERY, "filterSubjectCountByLibrarySource",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "library_selections",
                WIDGET_QUERY, "subjectCountByLibrarySelection",
                FILTER_COUNT_QUERY, "filterSubjectCountByLibrarySelection",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "library_layouts",
                WIDGET_QUERY, "subjectCountByLibraryLayout",
                FILTER_COUNT_QUERY, "filterSubjectCountByLibraryLayout",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "platforms",
                WIDGET_QUERY, "subjectCountByPlatform",
                FILTER_COUNT_QUERY, "filterSubjectCountByPlatform",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "instrument_models",
                WIDGET_QUERY, "subjectCountByInstrumentModel",
                FILTER_COUNT_QUERY, "filterSubjectCountByInstrumentModel",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "reference_genome_assemblies",
                WIDGET_QUERY, "subjectCountByReferenceGenomeAssembly",
                FILTER_COUNT_QUERY, "filterSubjectCountByReferenceGenomeAssembly",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "primary_diagnoses",
                WIDGET_QUERY, "subjectCountByPrimaryDiagnosis",
                FILTER_COUNT_QUERY, "filterSubjectCountByPrimaryDiagnosis",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "phs_accession",
                WIDGET_QUERY, "subjectCountByPhsAccession",
                FILTER_COUNT_QUERY, "filterSubjectCountByPhsAccession",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "study_data_types",
                WIDGET_QUERY, "subjectCountByStudyDataType",
                FILTER_COUNT_QUERY, "filterSubjectCountByStudyDataType",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "acl",
                WIDGET_QUERY, "subjectCountByAcl",
                FILTER_COUNT_QUERY, "filterSubjectCountByAcl",
                AGG_ENDPOINT, FILES_END_POINT
        ));


        List<String> agg_names = new ArrayList<>();
        for (var agg: TERM_AGGS) {
            agg_names.add(agg.get(AGG_NAME));
        }
        final String[] TERM_AGG_NAMES = agg_names.toArray(new String[TERM_AGGS.size()]);

        final Map<String, String> RANGE_AGGS = new HashMap<>();
        RANGE_AGGS.put("number_of_study_participants",  "filterSubjectCountByNumberOfStudyParticipants");
        RANGE_AGGS.put("number_of_study_samples",  "filterSubjectCountByNumberOfStudySamples");
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
        Request fileRequest = new Request("GET", FILES_END_POINT);
        fileRequest.setJsonEntity(gson.toJson(aggQuery));
        JsonObject subjectResult = esService.send(fileRequest);
        Map<String, JsonArray> aggs = esService.collectTermAggs(subjectResult, TERM_AGG_NAMES);

        Map<String, Object> data = new HashMap<>();
        data.put("numberOfStudies", aggs.get("studies").size());
        data.put("numberOfSubjects", numberOfSubjects);
        data.put("numberOfSamples", numberOfSamples);
        data.put("numberOfFiles", numberOfFiles);
        data.put("numberOfDiseaseSites", aggs.get("site").size());

        // widgets data and facet filter counts
        for (var agg: TERM_AGGS) {
            String field = agg.get(AGG_NAME);
            String widgetQueryName = agg.get(WIDGET_QUERY);
            String filterCountQueryName = agg.get(FILTER_COUNT_QUERY);
            String endpoint = agg.get(AGG_ENDPOINT);
            // subjectCountByXXXX
            List<Map<String, Object>> widgetData;
            if (endpoint.equals(FILES_END_POINT)) {
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
                new String[]{"study_acronym", "studies"},
                new String[]{"phs_accession", "phs_accession"},
                new String[]{"gender", "genders"},
                new String[]{"site", "site"},
                new String[]{"samples", "samples"},
                new String[]{"files", "files"},
                new String[]{"analyte_type", "analyte_type"}
        };

        String defaultSort = "subject_ids"; // Default sort order

        Map<String, String> sortFieldMapping = Map.ofEntries(
                Map.entry("subject_id", "subject_ids"),
                Map.entry("study_acronym", "studies"),
                Map.entry("phs_accession", "phs_accession"),
                Map.entry("gender", "genders"),
                Map.entry("site", "site"),
                Map.entry("analyte_type", "analyte_type")
        );

        return overview(SUBJECTS_END_POINT, params, PROPERTIES, defaultSort, sortFieldMapping);
    }

    private List<Map<String, Object>> sampleOverview(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"study_acronym", "studies"},
                new String[]{"phs_accession", "phs_accession"},
                new String[]{"subject_id", "subject_ids"},
                new String[]{"sample_id", "sample_id"},
                new String[]{"is_tumor", "is_tumor"},
                new String[]{"analyte_type", "analyte_type"},
                new String[]{"files", "files"}
        };

        String defaultSort = "sample_id"; // Default sort order

        Map<String, String> sortFieldMapping = Map.ofEntries(
                Map.entry("study_acronym", "studies"),
                Map.entry("phs_accession", "phs_accession"),
                Map.entry("subject_id", "subject_ids"),
                Map.entry("sample_id", "sample_id"),
                Map.entry("is_tumor", "is_tumor"),
                Map.entry("analyte_type", "analyte_type")
        );

        return overview(SAMPLES_END_POINT, params, PROPERTIES, defaultSort, sortFieldMapping);
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        final String[][] PROPERTIES = new String[][]{
                new String[]{"study_acronym", "studies"},
                new String[]{"accesses", "accesses"},
                new String[]{"phs_accession", "phs_accession"},
                new String[]{"subject_id", "subject_ids"},
                new String[]{"sample_id", "sample_id"},
                new String[]{"experimental_strategy", "experimental_strategies"},
                new String[]{"gender", "genders"},
                new String[]{"analyte_type", "analyte_type"},
                new String[]{"is_tumor", "is_tumor"},
                new String[]{"file_name", "file_name"},
                new String[]{"file_type", "file_type"},
                new String[]{"file_size", "file_size"},
                new String[]{"file_id", "file_id"},
                new String[]{"md5sum", "md5sum"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> sortFieldMapping = Map.ofEntries(
                Map.entry("study_acronym", "studies"),
                Map.entry("accesses", "accesses"),
                Map.entry("phs_accession", "phs_accession"),
                Map.entry("subject_id", "subject_ids"),
                Map.entry("sample_id", "sample_id"),
                Map.entry("experimental_strategy", "experimental_strategies"),
                Map.entry("gender", "genders"),
                Map.entry("analyte_type", "analyte_type"),
                Map.entry("is_tumor", "is_tumor"),
                Map.entry("file_name", "file_name"),
                Map.entry("file_type", "file_type"),
                Map.entry("file_size", "file_size"),
                Map.entry("file_id", "file_id"),
                Map.entry("md5sum", "md5sum")
        );

        return overview(FILES_END_POINT, params, PROPERTIES, defaultSort, sortFieldMapping);
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
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(), Set.of(PAGE_SIZE));
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

    private List<Map<String, Object>> subjectCountBy(String category, Map<String, Object> params, String endpoint) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(), Set.of(PAGE_SIZE));
        return getGroupCount(category, query, endpoint);
    }

    private List<Map<String, Object>> filterSubjectCountBy(String category, Map<String, Object> params, String endpoint) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(), Set.of(PAGE_SIZE, category));
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
                    "subjects", group.getAsJsonObject().get("doc_count").getAsInt()
            ));

        }
        return data;
    }

    private Map<String, Object> rangeFilterSubjectCountBy(String category, Map<String, Object> params) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, Set.of(), Set.of(PAGE_SIZE, category));
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
        final String LOWER_BOUND = "lowerBound";
        final String UPPER_BOUND = "upperBound";
        Map<String, Object> range = new HashMap<>();
        range.put("subjects", aggs.get("count").getAsInt());
        JsonElement lowerBound = aggs.get("min");
        if (!lowerBound.isJsonNull()) {
            range.put(LOWER_BOUND, lowerBound.getAsDouble());
        } else {
            range.put(LOWER_BOUND, null);
        }
        JsonElement upperBound = aggs.get("max");
        if (!upperBound.isJsonNull()) {
            range.put("upperBound", aggs.get("max").getAsDouble());
        } else {
            range.put(UPPER_BOUND, null);
        }

        return range;
    }

    private Map<String, Object> globalSearch(Map<String, Object> params) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String input = (String) params.get("input");
        int size = (int) params.get("first");
        int offset = (int) params.get("offset");
        List<Map<String, Object>> searchCategories = new ArrayList<>();
        searchCategories.add(Map.of(
                GS_END_POINT, STUDIES_END_POINT,
                GS_COUNT_ENDPOINT, STUDIES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "study_count",
                GS_RESULT_FIELD, "studies",
                GS_SEARCH_FIELD, List.of("phs_accession_gs", "study_name", "study_code"),
                GS_SORT_FIELD, "phs_accession",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"phs_accession", "phs_accession_gs"},
                        new String[]{"study_code", "study_code"},
                        new String[]{"study_name", "study_name"}
                },
                GS_CATEGORY_TYPE, "study"

        ));
        searchCategories.add(Map.of(
                GS_END_POINT, SUBJECTS_END_POINT,
                GS_COUNT_ENDPOINT, SUBJECTS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "subject_count",
                GS_RESULT_FIELD, "subjects",
                GS_SEARCH_FIELD, List.of("subject_id_gs", "site_gs"),
                GS_SORT_FIELD, "subject_ids",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"study", "study_gs"},
                        new String[]{"subject_id", "subject_id_gs"},
                        new String[]{"site", "site_gs"}
                },
                GS_CATEGORY_TYPE, "subject"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, SAMPLES_END_POINT,
                GS_COUNT_ENDPOINT, SAMPLES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "sample_count",
                GS_RESULT_FIELD, "samples",
                GS_SEARCH_FIELD, List.of("sample_id_gs", "is_tumor_gs", "analyte_type_gs"),
                GS_SORT_FIELD, "sample_id",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"subject_id", "subject_id"},
                        new String[]{"sample_id", "sample_id"},
                        new String[]{"is_tumor", "is_tumor"},
                        new String[]{"analyte_type", "analyte_type"}
                },
                GS_CATEGORY_TYPE, "sample"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, FILES_END_POINT,
                GS_COUNT_ENDPOINT, FILES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "file_count",
                GS_RESULT_FIELD, "files",
                GS_SEARCH_FIELD, List.of("file_id_gs", "file_name_gs", "file_type_gs"),
                GS_SORT_FIELD, "file_id",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"subject_id", "subject_ids"},
                        new String[]{"sample_id", "sample_id"},
                        new String[]{"file_id", "file_id"},
                        new String[]{"file_name", "file_name"},
                        new String[]{"file_type", "file_type"}
                },
                GS_CATEGORY_TYPE, "file"
        ));
        searchCategories.add(Map.of(
                GS_END_POINT, PROGRAMS_END_POINT,
                GS_COUNT_ENDPOINT, PROGRAMS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "program_count",
                GS_RESULT_FIELD, "programs",
                GS_SEARCH_FIELD, List.of("program_name", "program_short_description", "program_full_description",
                        "program_external_url", "program_sort_order"),
                GS_SORT_FIELD, "program_sort_order_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"program_name", "program_name"},
                        new String[]{"program_short_description", "program_short_description"},
                        new String[]{"program_full_description", "program_full_description"},
                        new String[]{"program_external_url", "program_external_url"},
                        new String[]{"program_sort_order", "program_sort_order"},
                        new String[]{"type", "type"}
                },
                GS_CATEGORY_TYPE, "program"
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

    private List<Map<String, Object>> findSubjectIdsInList(Map<String, Object> params) throws IOException {
        final String[][] properties = new String[][]{
                new String[]{"subject_id", "subject_id"},
                new String[]{"phs_accession", "phs_accession"}
        };

        Map<String, Object> query = esService.buildListQuery(params, Set.of(), true);
        Request request = new Request("GET", SUBJECT_IDS_END_POINT);

        return esService.collectPage(request, query, properties, ESService.MAX_ES_SIZE, 0);
    }

    private List<Map<String, Object>> filesInList(Map<String, Object> params) throws IOException {
        final String[][] properties = new String[][]{
                new String[]{"study_acronym", "studies"},
                new String[]{"subject_id", "subject_ids"},
                new String[]{"sample_id", "sample_id"},
                new String[]{"file_name", "file_name"},
                new String[]{"file_type", "file_type"},
                new String[]{"file_size", "file_size"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> sortFieldMapping = Map.ofEntries(
                Map.entry("study_acronym", "studies"),
                Map.entry("subject_id", "subject_ids"),
                Map.entry("sample_id", "sample_id"),
                Map.entry("file_name", "file_name"),
                Map.entry("file_type", "file_type"),
                Map.entry("file_size", "file_size")
        );

        Map<String, Object> query = esService.buildListQuery(params, Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
        String order_by = (String)params.get(ORDER_BY);
        String direction = ((String)params.get(SORT_DIRECTION)).toLowerCase();
        query.put("sort", mapSortOrder(order_by, direction, defaultSort, sortFieldMapping));
        int pageSize = (int) params.get(PAGE_SIZE);
        int offset = (int) params.get(OFFSET);
        Request request = new Request("GET", FILES_END_POINT);

        return esService.collectPage(request, query, properties, pageSize, offset);
    }

    private List<String> fileIDsFromList(Map<String, Object> params) throws IOException {
        return collectFieldFromList(params, "file_id", FILES_END_POINT);
    }

    // This function search values in parameters and return a given collectField's unique values in a list
    private List<String> collectFieldFromList(Map<String, Object> params, String collectField, String endpoint) throws IOException {
        String[] idFieldArray = new String[]{collectField};
        Map<String, Object> query = esService.buildListQuery(params, Set.of());
        query = esService.addAggregations(query, idFieldArray);
        Request request = new Request("GET", endpoint);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        return esService.collectTerms(jsonObject, collectField);
    }
}
