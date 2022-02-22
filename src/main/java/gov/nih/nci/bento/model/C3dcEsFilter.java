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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class C3dcEsFilter implements DataFetcher{
    private static final Logger logger = LogManager.getLogger(BentoEsFilter.class);

    //TODO populate endpoint variables
    final String SUBJECTS_END_POINT = "/subject_characteristics/_search";
    final String SUBJECTS_COUNT_END_POINT = "/subject_characteristics/_count";
    final String FILES_END_POINT = "/files/_search";
    final String FILES_COUNT_END_POINT = "/files/_count";

    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";
    final String SORT_DIRECTION = "sort_direction";

    final String AGG_NAME = "agg_name";
    final String AGG_ENDPOINT = "agg_endpoint";
    final String WIDGET_QUERY = "widgetQueryName";
    final String FILTER_COUNT_QUERY = "filterCountQueryName";

    final Set<String> RANGE_PARAMS = Set.of();


    @Autowired
    ESService esService;

    private final Gson gson = new GsonBuilder().serializeNulls().create();

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
                )
                .build();
    }

    private List<Map<String, Object>> subjectOverview(Map<String, Object> params) throws IOException {
        final String[][] PROPERTIES = new String[][]{
                new String[]{"pcdc_subject_id", "pcdc_subject_id"},
                new String[]{"race", "race"},
                new String[]{"sex", "sex"},
                new String[]{"disease_phase", "disease_phase"},
                new String[]{"treatment_arm", "treatment_arm"},
                new String[]{"disease_site", "disease_site"},
                new String[]{"data_contributor_id", "data_contributor_id"},
                new String[]{"ae_grade", "ae_grade"},
                new String[]{"ae_outcome", "ae_outcome"},
                new String[]{"study_id", "study_id"},
                new String[]{"program_id", "program_id"},
                new String[]{"cancer", "cancer"}
        };

        String defaultSort = "pcdc_subject_id"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("pcdc_subject_id", "pcdc_subject_id"),
                Map.entry("race", "race"),
                Map.entry("sex", "sex"),
                Map.entry("disease_phase", "disease_phase"),
                Map.entry("treatment_arm", "treatment_arm"),
                Map.entry("disease_site", "disease_site"),
                Map.entry("data_contributor_id", "data_contributor_id"),
                Map.entry("ae_grade", "ae_grade"),
                Map.entry("ae_outcome", "ae_outcome"),
                Map.entry("study_id", "study_id"),
                Map.entry("program_id", "program_id"),
                Map.entry("cancer", "cancer")
        );

        return overview(SUBJECTS_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
        final String[][] PROPERTIES = new String[][]{
                new String[]{"pcdc_subject_id", "pcdc_subject_id"},
                new String[]{"race", "race"},
                new String[]{"sex", "sex"},
                new String[]{"disease_phase", "disease_phase"},
                new String[]{"treatment_arm", "treatment_arm"},
                new String[]{"disease_site", "disease_site"},
                new String[]{"data_contributor_id", "data_contributor_id"},
                new String[]{"ae_grade", "ae_grade"},
                new String[]{"ae_outcome", "ae_outcome"},
                new String[]{"study_id", "study_id"},
                new String[]{"program_id", "program_id"},
                new String[]{"file_name", "file_name"},
                new String[]{"file_type", "file_type"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"md5sum", "md5sum"},
                new String[]{"file_status", "file_status"},
                new String[]{"uuid", "uuid"},
                new String[]{"file_location", "file_location"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("pcdc_subject_id", "pcdc_subject_id"),
                Map.entry("race", "race"),
                Map.entry("sex", "sex"),
                Map.entry("disease_phase", "disease_phase"),
                Map.entry("treatment_arm", "treatment_arm"),
                Map.entry("disease_site", "disease_site"),
                Map.entry("data_contributor_id", "data_contributor_id"),
                Map.entry("ae_grade", "ae_grade"),
                Map.entry("ae_outcome", "ae_outcome"),
                Map.entry("study_id", "study_id"),
                Map.entry("program_id", "program_id"),
                Map.entry("file_name", "file_name"),
                Map.entry("file_type", "file_type"),
                Map.entry("file_description", "file_description"),
                Map.entry("file_format", "file_format"),
                Map.entry("file_size", "file_size"),
                Map.entry("md5sum", "md5sum"),
                Map.entry("file_status", "file_status"),
                Map.entry("uuid", "uuid"),
                Map.entry("file_location", "file_location")
        );

        return overview(FILES_END_POINT, params, PROPERTIES, defaultSort, mapping);
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException, InterruptedException {
        final String NUM_SUBJECTS = "numberOfSubjects";
        final String NUM_PROGRAMS = "numberOfPrograms";
        final String NUM_FILES = "numberOfFiles";
        final String NUM_STUDIES = "numberOfStudies";
        final List<String> varsToRemove = Arrays.asList("first", "offset", "order_by", "sort_direction");
        Map<String,Object> filterParams = new HashMap<>();
        for (String key: params.keySet()) {
            if (!varsToRemove.contains(key)) {
                filterParams.put(key, params.get(key));
            }
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Request> map = Map.of(
                //TODO count studies and programs
                NUM_SUBJECTS, new Request("GET", SUBJECTS_COUNT_END_POINT),
                NUM_FILES, new Request("GET", FILES_COUNT_END_POINT)
        );

        Map<String, Object> query = esService.buildFacetFilterQuery(filterParams, RANGE_PARAMS);
        GetResultType type = (json)-> json.get("count").getAsInt();
        map.forEach((k, request)->{
            request.setJsonEntity(gson.toJson(query));
            result.put(k, searchES(request, type));
        });

        Request request = new Request("GET", SUBJECTS_END_POINT);
        request.setJsonEntity(gson.toJson(getSubjectQuery(query)));
        JsonObject subjectResult = (JsonObject) searchES(request, (j)-> j);

        final List<Map<String, String>> TERM_AGGS = GetTermsAggregations();
        Map<String, JsonArray> aggs = esService.collectTermAggs(subjectResult, getTermAggNames(TERM_AGGS));

        result.put(NUM_PROGRAMS, aggs.get("program_id").size());
        result.put(NUM_STUDIES, aggs.get("study_id").size());

        // widgets data and facet filter counts
        addWidgetData(result, TERM_AGGS, filterParams, aggs);

        Map<String, JsonObject> rangeAggs = esService.collectRangeAggs(subjectResult, getSubjectMapRange());
        addFilterCountData(result, rangeAggs, filterParams);
        return result;
    }

    private void addFilterCountData(Map<String, Object> data, Map<String, JsonObject> rangeAggs, Map<String, Object> params) throws IOException {
        Map<String, String> RANGE_AGGS = getSubjectMap();
        String[] RANGE_AGG_NAMES = getSubjectMapRange();
        for (String field: RANGE_AGG_NAMES) {
            String filterCountQueryName = RANGE_AGGS.get(field);
            if (params.containsKey(field) && ((List<Double>)params.get(field)).size() >= 2) {
                Map<String, Object> filterCount = rangeFilterSubjectCountBy(field, params);
                data.put(filterCountQueryName, filterCount);
            } else {
                data.put(filterCountQueryName, getRange(rangeAggs.get(field)));
            }
        }
    }

    private List<Map<String, Object>> getGroupCountHelper(JsonArray buckets) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (JsonElement group: buckets) {
            data.add(Map.of("group", group.getAsJsonObject().get("key").getAsString(),
                    "subjects", group.getAsJsonObject().get("doc_count").getAsInt()
            ));
        }
        return data;
    }

    private void addWidgetData(Map<String, Object> data, List<Map<String, String>> TERM_AGGS, Map<String, Object> params, Map<String, JsonArray> aggs) throws IOException, InterruptedException {
        for (var agg: TERM_AGGS) {
            String field = agg.get(AGG_NAME);
            String widgetQueryName = agg.get(WIDGET_QUERY);
            String filterCountQueryName = agg.get(FILTER_COUNT_QUERY);
            String endpoint = agg.get(AGG_ENDPOINT);
            // subjectCountByXXXX
            List<Map<String, Object>> widgetData = endpoint.equals(SUBJECTS_END_POINT) ? getGroupCountHelper(aggs.get(field)) : subjectCountBy(field, params, endpoint);
            data.put(widgetQueryName, widgetData);

            // filterSubjectCountByXXXX
            if (params.containsKey(field) && ((List<String>)params.get(field)).size() > 0) {
                List<Map<String, Object>> filterCount = filterSubjectCountBy(field, params, endpoint);
                data.put(filterCountQueryName, filterCount);
            } else {
                data.put(filterCountQueryName, widgetData);
            }
        }
    }

    private List<Map<String, Object>> subjectCountBy(String category, Map<String, Object> params, String endpoint) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE));
        return getGroupCount(category, query, endpoint);
    }

    private List<Map<String, Object>> getGroupCount(String category, Map<String, Object> query, String endpoint) throws IOException {
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        JsonArray buckets = getESBuckets(category, query, endpoint);
        return getGroupCountHelper(buckets);
    }

    private JsonArray getESBuckets(String category, Map<String, Object> query, String endpoint) throws IOException {
        Request request = new Request("GET", endpoint);
        request.setJsonEntity(gson.toJson(query));
        JsonObject jsonObject = esService.send(request);
        Map<String, JsonArray> aggs = esService.collectTermAggs(jsonObject, new String[] {category});
        return aggs.get(category);
    }

    private List<Map<String, Object>> filterSubjectCountBy(String category, Map<String, Object> params, String endpoint) throws IOException {
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE, category));
        return getGroupCount(category, query, endpoint);
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

    private List<Map<String, Object>> overview(String endpoint, Map<String, Object> params, String[][] properties, String defaultSort, Map<String, String> mapping) throws IOException {

        Request request = new Request("GET", endpoint);
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
        String order_by = (String)params.get(ORDER_BY);
        String direction = ((String)params.get(SORT_DIRECTION)).toLowerCase();
        query.put("sort", mapSortOrder(order_by, direction, defaultSort, mapping));
        int pageSize = (int) params.get(PAGE_SIZE);
        int offset = (int) params.get(OFFSET);
        return esService.collectPage(request, query, properties, pageSize, offset);
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

    private List<Map<String, String>> GetTermsAggregations() {
        // Query related values
        final List<Map<String, String>> TERM_AGGS = new ArrayList<>();
        TERM_AGGS.add(Map.of(
                AGG_NAME, "program_id",
                WIDGET_QUERY, "subjectCountByProgram",
                FILTER_COUNT_QUERY, "filterSubjectCountByProgram",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "study_id",
                WIDGET_QUERY, "subjectCountByStudy",
                FILTER_COUNT_QUERY, "filterSubjectCountByStudy",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "data_contributor_id",
                WIDGET_QUERY, "subjectCountByDataContributorId",
                FILTER_COUNT_QUERY, "filterSubjectCountByDataContributor",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "sex",
                WIDGET_QUERY, "subjectCountBySex",
                FILTER_COUNT_QUERY, "filterSubjectCountBySex",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "race",
                WIDGET_QUERY, "subjectCountByRace",
                FILTER_COUNT_QUERY, "filterSubjectCountByRace",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "disease_phase",
                WIDGET_QUERY, "subjectCountByDiseasePhase",
                FILTER_COUNT_QUERY, "filterSubjectCountByDiseasePhase",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "ae_grade",
                WIDGET_QUERY, "subjectCountByAEGrade",
                FILTER_COUNT_QUERY, "filterSubjectCountByAEGrade",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "ae_outcome",
                WIDGET_QUERY, "subjectCountByAEOutcome",
                FILTER_COUNT_QUERY, "filterSubjectCountByAEOutcome",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "disease_site",
                WIDGET_QUERY, "subjectCountByDiseaseSite",
                FILTER_COUNT_QUERY, "filterSubjectCountByDiseaseSite",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        return TERM_AGGS;
    }

    @FunctionalInterface
    private interface GetResultType<T> {
        T get(JsonObject obj);
    }

    private Object searchES(Request request, GetResultType type) {
        JsonObject json = send(request);
        return type.get(json);
    }

    private JsonObject send(Request r) {
        JsonObject json = null;
        try {
            json = esService.send(r);
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return json;
    }

    private Map<String, Object> getSubjectQuery(Map<String, Object> query) {
        // Query related values
        final List<Map<String, String>> TERM_AGGS = GetTermsAggregations();
        final String[] TERM_AGG_NAMES = getTermAggNames(TERM_AGGS);
        return esService.addAggregations(query, TERM_AGG_NAMES, getSubjectMapRange());
    }

    private String[] getTermAggNames(List<Map<String, String>> TERM_AGGS) {
        // Get aggregations
        List<String> agg_names = new ArrayList<>();
        for (var agg: TERM_AGGS) agg_names.add(agg.get(AGG_NAME));
        return agg_names.toArray(new String[TERM_AGGS.size()]);
    }

    private String[] getSubjectMapRange() {
        return getSubjectMap().keySet().toArray(new String[0]);
    }

    private Map<String, String> getSubjectMap() {
        final Map<String, String> RANGE_AGGS = new HashMap<>();
        RANGE_AGGS.put("age_at_index",  "filterSubjectCountByAge");
        return RANGE_AGGS;
    }



}
