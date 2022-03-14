package gov.nih.nci.bento.model;

import com.google.gson.*;
import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.classes.QueryResult;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.BENTO_INDEX;
import gov.nih.nci.bento.constants.Const.ES_UNITS;
import gov.nih.nci.bento.model.search.query.AggregationFilter;
import gov.nih.nci.bento.model.search.query.RangeFilter;
import gov.nih.nci.bento.model.search.query.SearchCountFilter;
import gov.nih.nci.bento.model.search.query.SubAggregationFilter;
import gov.nih.nci.bento.service.ESServiceImpl;
import gov.nih.nci.bento.utility.StrUtil;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@RequiredArgsConstructor
public class BentoEsSearch implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(BentoEsSearch.class);

    // parameters used in queries
    final String PAGE_SIZE = "first";
    final String OFFSET = "offset";
    final String ORDER_BY = "order_by";
    final String SORT_DIRECTION = "sort_direction";

    final String PROGRAMS_END_POINT = "/programs/_search";
    final String PROGRAMS_COUNT_END_POINT = "/programs/_count";
    final String STUDIES_END_POINT = "/studies/_search";
    final String STUDIES_COUNT_END_POINT = "/studies/_count";

    final String SUBJECTS_END_POINT = "/subjects/_search";
    final String SUBJECTS_COUNT_END_POINT = "/subjects/_count";
    final String SUBJECT_IDS_END_POINT = "/subject_ids/_search";
    final String SAMPLES_END_POINT = "/samples/_search";
    final String SAMPLES_COUNT_END_POINT = "/samples/_count";
    final String FILES_END_POINT = "/files/_search";
    final String FILES_COUNT_END_POINT = "/files/_count";
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
    final Set<String> RANGE_PARAMS = Set.of("age_at_index");

    @Autowired
    ESServiceImpl esService;

    public final TypeMapperImpl typeMapper;

    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchSubjects", env -> {

                            return multiSearchTest(esService.CreateQueryParam(env));
//                            Map<String, Object> args = env.getArguments();
//                            return searchSubjects(args);
                        })
                        .dataFetcher("subjectOverview", env -> {
//                            Map<String, Object> args = env.getArguments();
                            return subjectOverview_Test(esService.CreateQueryParam(env));
                        })
                        .dataFetcher("sampleOverview", env -> {
//                            Map<String, Object> args = env.getArguments();
//                            return sampleOverview(args);

                            return sampleOverviewTest(esService.CreateQueryParam(env));
                        })
                        .dataFetcher("fileOverview", env -> {
//                            Map<String, Object> args = env.getArguments();
//                            return fileOverview(args);
                            return fileOverviewTest(esService.CreateQueryParam(env));

                        })
                        .dataFetcher("globalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                        .dataFetcher("fileIDsFromList", env -> {
//                            Map<String, Object> args = env.getArguments();
//                            return fileIDsFromList(args);
                            return fileIDsFromListTest(esService.CreateQueryParam(env));
                        })
                        .dataFetcher("filesInList", env -> {
//                            Map<String, Object> args = env.getArguments();
//                            return filesInList(args);
                            return filesInListTest(esService.CreateQueryParam(env));
                        })
                        .dataFetcher("findSubjectIdsInList", env -> {
//                            Map<String, Object> args = env.getArguments();
//                            return findSubjectIdsInList(args);
                            return findSubjectIdsInListTest(esService.CreateQueryParam(env));
                        })
                )
                .build();
    }

    private List<Map<String, String>> GetTermsAggregations() {
        // Query related values
        final List<Map<String, String>> TERM_AGGS = new ArrayList<>();
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "programs",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByProgram",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByProgram",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "studies",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByStudy",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByStudy",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "diagnoses",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByDiagnoses",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByDiagnoses",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "rc_scores",
                Const.ES_FILTER.WIDGET_QUERY,"subjectCountByRecurrenceScore",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByRecurrenceScore",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "tumor_sizes",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByTumorSize",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByTumorSize",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "tumor_grades",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByTumorGrade",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByTumorGrade",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "er_status",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByErStatus",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByErStatus",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "pr_status",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByPrStatus",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByPrStatus",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "chemo_regimen",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByChemotherapyRegimen",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByChemotherapyRegimen",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "endo_therapies",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByEndocrineTherapy",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByEndocrineTherapy",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "meno_status",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByMenopauseStatus",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByMenopauseStatus",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "tissue_type",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByTissueType",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByTissueType",
                Const.ES_FILTER.AGG_ENDPOINT, SAMPLES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "composition",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByTissueComposition",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByTissueComposition",
                Const.ES_FILTER.AGG_ENDPOINT, SAMPLES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "association",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByFileAssociation",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByFileAssociation",
                Const.ES_FILTER.AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "file_type",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByFileType",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByFileType",
                Const.ES_FILTER.AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                Const.ES_FILTER.AGG_NAME, "lab_procedures",
                Const.ES_FILTER.WIDGET_QUERY, "subjectCountByLabProcedures",
                Const.ES_FILTER.FILTER_COUNT_QUERY, "filterSubjectCountByLabProcedures",
                Const.ES_FILTER.AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        return TERM_AGGS;
    }

    private Map<String, String> getSubjectMap() {
        final Map<String, String> RANGE_AGGS = new HashMap<>();
        RANGE_AGGS.put("age_at_index",  "filterSubjectCountByAge");
        return RANGE_AGGS;
    }

    private String[] getSubjectMapRange() {
        return getSubjectMap().keySet().toArray(new String[0]);
    }


    private String[] getTermAggNames(List<Map<String, String>> TERM_AGGS) {
        // Get aggregations
        List<String> agg_names = new ArrayList<>();
        for (var agg: TERM_AGGS) agg_names.add(agg.get(Const.ES_FILTER.AGG_NAME));
        return agg_names.toArray(new String[TERM_AGGS.size()]);
    }

    private Map<String, Object> getSubjectQuery(Map<String, Object> query) {
        // Query related values
        final List<Map<String, String>> TERM_AGGS = GetTermsAggregations();
        final String[] TERM_AGG_NAMES = getTermAggNames(TERM_AGGS);
        return esService.addAggregations(query, TERM_AGG_NAMES, getSubjectMapRange());
    }

    @FunctionalInterface
    private interface GetResultType<T> {
        T get(JsonObject obj);
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

    private Object searchES(Request request, GetResultType type) {
        JsonObject json = send(request);
        return type.get(json);
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException, InterruptedException {

        Map<String, Object> result = new HashMap<>();

        Map<String, Request> map = Map.of(
                Bento_GraphQL_KEYS.NO_OF_SUBJECTS, new Request("GET", SAMPLES_COUNT_END_POINT),
                Bento_GraphQL_KEYS.NO_OF_SAMPLES, new Request("GET", FILES_COUNT_END_POINT),
                Bento_GraphQL_KEYS.NO_OF_FILES, new Request("GET", SUBJECTS_COUNT_END_POINT)
        );

        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS);
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

        result.put(Bento_GraphQL_KEYS.NO_OF_PROGRAMS, aggs.get("programs").size());
        result.put(Bento_GraphQL_KEYS.NO_OF_STUDIES, aggs.get("studies").size());
        result.put(Bento_GraphQL_KEYS.NO_OF_LAB_PROCEDURES, aggs.get("lab_procedures").size());
        result.put(Bento_GraphQL_KEYS.NO_OF_ARMS_PROGRAM, armsByPrograms(params));
        // widgets data and facet filter counts
        addWidgetData(result, TERM_AGGS, params, aggs);

        Map<String, JsonObject> rangeAggs = esService.collectRangeAggs(subjectResult, getSubjectMapRange());
        addFilterCountData(result, rangeAggs, params);
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

    private void addWidgetData(Map<String, Object> data, List<Map<String, String>> TERM_AGGS, Map<String, Object> params, Map<String, JsonArray> aggs) throws IOException, InterruptedException {
        for (var agg: TERM_AGGS) {
            String field = agg.get(Const.ES_FILTER.AGG_NAME);
            String widgetQueryName = agg.get(Const.ES_FILTER.WIDGET_QUERY);
            String filterCountQueryName = agg.get(Const.ES_FILTER.FILTER_COUNT_QUERY);
            String endpoint = agg.get(Const.ES_FILTER.AGG_ENDPOINT);
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

    private List<Map<String, Object>> subjectOverview_Test(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(
                esService.createPageSourceBuilder(param, BENTO_FIELDS.SUBJECT_ID_NUM)
        );
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
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

    private List<Map<String, Object>> sampleOverviewTest(QueryParam param) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SAMPLES);
        request.source(
                // TODO Add Test Case
                esService.createPageSourceBuilder(param, BENTO_FIELDS.SAMPLE_ID_NUM+ ES_UNITS.KEYWORD)
        );
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
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

    private List<Map<String, Object>> fileOverviewTest(QueryParam param) throws IOException {
        // Set Rest API Request
        // TODO add test case
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES);
        request.source(
                esService.createPageSourceBuilder(param,BENTO_FIELDS.FILE_NAME+ ES_UNITS.KEYWORD)
        );
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }


    private List<Map<String, Object>> fileOverview(Map<String, Object> params) throws IOException {
        // Following String array of arrays should be in form of "GraphQL_field_name", "ES_field_name"
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

    private List<Map<String, Object>> armsByPrograms(Map<String, Object> params) throws IOException {
        final String category = "programs";
        final String subCategory = "study_acronym";

        String[] subCategories = new String[] { subCategory };
        Map<String, Object> query = esService.buildFacetFilterQuery(params, RANGE_PARAMS, Set.of(PAGE_SIZE));
        String[] AGG_NAMES = new String[] {category};
        query = esService.addAggregations(query, AGG_NAMES);
        esService.addSubAggregations(query, category, subCategories);
        JsonArray buckets = getESBuckets(category, query, SUBJECTS_END_POINT);

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

    private List<Map<String, Object>> getGroupCountHelper(JsonArray buckets) {
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


    private List<Map<String, Object>> GetSearchCategories() {
        // DONE
        List<Map<String, Object>> searchCategories = new ArrayList<>();
        searchCategories.add(Map.of(
                GS_END_POINT, PROGRAMS_END_POINT,
                GS_COUNT_ENDPOINT, PROGRAMS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "program_count",
                GS_RESULT_FIELD, "programs",
                GS_SEARCH_FIELD, List.of("program_id", "program_code", "program_name"),
                GS_SORT_FIELD, "program_id_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"program_code", "program_code"},
                        new String[]{"program_id", "program_id"},
                        new String[]{"program_name", "program_name"}
                },
                GS_CATEGORY_TYPE, "program"
        ));
        // Done
        searchCategories.add(Map.of(
                GS_END_POINT, STUDIES_END_POINT,
                GS_COUNT_ENDPOINT, STUDIES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "study_count",
                GS_RESULT_FIELD, "studies",
                GS_SEARCH_FIELD, List.of("study_id", "study_name", "study_type"),
                GS_SORT_FIELD, "study_id_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"program_id", "program_id"},
                        new String[]{"study_id", "study_id"},
                        new String[]{"study_type", "study_type"},
                        new String[]{"study_code", "study_code"},
                        new String[]{"study_name", "study_name"}
                },
                GS_CATEGORY_TYPE, "study"

        ));
        // Done
        searchCategories.add(Map.of(
                GS_END_POINT, SUBJECTS_END_POINT,
                GS_COUNT_ENDPOINT, SUBJECTS_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "subject_count",
                GS_RESULT_FIELD, "subjects",
//                GS_SEARCH_FIELD, List.of("subject_id_gs", "diagnosis_gs"),
                // TODO
                GS_SEARCH_FIELD, List.of("subject_id_gs", "diagnosis_gs", "age_at_index_gs"),
                GS_SORT_FIELD, "subject_id_num",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"program_id", "program_id"},
                        new String[]{"subject_id", "subject_id_gs"},
                        new String[]{"program_code", "program"},
                        new String[]{"study", "study_acronym"},
                        new String[]{"diagnosis", "diagnosis"},
                        new String[]{"age", "age_at_index"}
                },
                GS_CATEGORY_TYPE, "subject"
        ));
        // DONE
        searchCategories.add(Map.of(
                GS_END_POINT, SAMPLES_END_POINT,
                GS_COUNT_ENDPOINT, SAMPLES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "sample_count",
                GS_RESULT_FIELD, "samples",
                GS_SEARCH_FIELD, List.of("sample_id_gs", "sample_anatomic_site_gs", "tissue_type_gs"),
                GS_SORT_FIELD, "sample_id_num",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"program_id", "program_id"},
                        new String[]{"subject_id", "subject_id"},
                        new String[]{"sample_id", "sample_id"},
                        new String[]{"diagnosis", "diagnosis"},
                        new String[]{"sample_anatomic_site", "sample_anatomic_site"},
                        new String[]{"tissue_type", "tissue_type"}
                },
                GS_CATEGORY_TYPE, "sample"
        ));
        // DONE
        searchCategories.add(Map.of(
                GS_END_POINT, FILES_END_POINT,
                GS_COUNT_ENDPOINT, FILES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "file_count",
                GS_RESULT_FIELD, "files",
                GS_SEARCH_FIELD, List.of("file_id_gs", "file_name_gs", "file_format_gs"),
                GS_SORT_FIELD, "file_id_num",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"program_id", "program_id"},
                        new String[]{"subject_id", "subject_ids"},
                        new String[]{"sample_id", "sample_ids"},
                        new String[]{"file_name", "file_names"},
                        new String[]{"file_format", "file_format"},
                        new String[]{"file_id", "file_ids"}
                },
                GS_CATEGORY_TYPE, "file"
        ));
        // Done
        searchCategories.add(Map.of(
                GS_END_POINT, NODES_END_POINT,
                GS_COUNT_ENDPOINT, NODES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "model_count",
                GS_RESULT_FIELD, "model",
                GS_SEARCH_FIELD, List.of("node"),
                GS_SORT_FIELD, "node_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"node_name", "node_name"}
                },
                GS_HIGHLIGHT_FIELDS, new String[][] {
                        new String[]{"highlight", "node"}
                },
                GS_CATEGORY_TYPE, "node"
        ));
        // DONE
        searchCategories.add(Map.of(
                GS_END_POINT, PROPERTIES_END_POINT,
                GS_COUNT_ENDPOINT, PROPERTIES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "model_count",
                GS_RESULT_FIELD, "model",
//                GS_SEARCH_FIELD, List.of("property", "property_description", "property_type"),
                GS_SEARCH_FIELD, List.of("property", "property_description", "property_type", "property_required"),
                GS_SORT_FIELD, "property_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"node_name", "node_name"},
                        new String[]{"property_name", "property_name"},
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
        // Done
        searchCategories.add(Map.of(
                GS_END_POINT, VALUES_END_POINT,
                GS_COUNT_ENDPOINT, VALUES_COUNT_END_POINT,
                GS_COUNT_RESULT_FIELD, "model_count",
                GS_RESULT_FIELD, "model",
                GS_SEARCH_FIELD, List.of("value"),
                GS_SORT_FIELD, "value_kw",
                GS_COLLECT_FIELDS, new String[][]{
                        new String[]{"node_name", "node_name"},
                        new String[]{"property_name", "property_name"},
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
        return searchCategories;
    }


    private Map<String, Object> globalSearch_Test(QueryParam param) throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(0);
        Map<String, Object> args = param.getArgs();
        // Set Global Filter
        QueryBuilder query = esService.createBentoBoolFromParams(args);
        List<MultipleRequests> requests = List.of(
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.CASES)
//                                .source(builder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.SAMPLES)
//                                .source(builder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.FILES)
//                                .source(builder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.PROGRAMS)
//                                .source(builder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.STUDIES)
//                                .source(builder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.MODEL_NODES)
//                                .source(builder))
//                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.ABOUT_COUNT)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.ABOUT)
                                .source(builder))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.ABOUT_PAGE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.ABOUT)
                                .source(builder))
                        .typeMapper(typeMapper.getAboutPage()).build()
//                MultipleRequests.builder()
//                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
//                        .request(new SearchRequest()
//                                .indices(BENTO_INDEX.MODEL_PROPERTIES)
//                                .source(builder))
//                        .typeMapper(typeMapper.getIntTotal()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        return result;

    }

    private Map<String, Object> globalSearch(Map<String, Object> params) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String input = (String) params.get("input");
        int size = (int) params.get("first");
        int offset = (int) params.get("offset");
        List<Map<String, Object>> searchCategories = GetSearchCategories();
        Set<String> combinedCategories = Set.of("model") ;

        for (Map<String, Object> category: searchCategories) {
            String countResultFieldName = (String) category.get(GS_COUNT_RESULT_FIELD);
            String resultFieldName = (String) category.get(GS_RESULT_FIELD);
            // Subjects Done
            if (resultFieldName.equals("subjects")) continue;
            if (resultFieldName.equals("samples")) continue;
            if (resultFieldName.equals("programs")) continue;
            if (resultFieldName.equals("studies")) continue;
            if (resultFieldName.equals("files")) continue;
            if (resultFieldName.equals("model")) continue;

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
//          TODO ADD SORT TYPE
            //            String sortFieldName = (String)category.get(GS_SORT_FIELD);
//            query.put("sort", Map.of(sortFieldName, "asc"));
            query = addHighlight(query, category);

            if (combinedCategories.contains(resultFieldName)) {
                query.put("size", ESServiceImpl.MAX_ES_SIZE);
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

        // Set Bool Filter
        SearchSourceBuilder testBuilder01 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
//                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(
                        addConditionalQuery(
                        new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.SUBJECT_ID_GS, input).boost((float) 1.50))
                        .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.DIGNOSIS_GS, List.of(input))),
                        // Set Conditional Integer Query
                        QueryBuilders.matchQuery(Const.BENTO_FIELDS.AGE_AT_INDEX,StrUtil.getIntText(input)))
                );

        SearchSourceBuilder testBuilder02 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
//                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(new BoolQueryBuilder()
                                .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.SAMPLE_ID_GS, input).boost((float) 1.50))
                                .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE_GS + ES_UNITS.KEYWORD, List.of(input)))
                                .should(QueryBuilders.termsQuery(Const.BENTO_FIELDS.TISSUE_TYPE_GS + ES_UNITS.KEYWORD, List.of(input)))
                );

        SearchSourceBuilder testBuilder03 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
//                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + ES_UNITS.KEYWORD)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROGRAM_ID, "*" + input + "*" ).boost((float) 1.50))
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROGRAM_CODE, input))
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROGRAM_NAME, input))
                );

        SearchSourceBuilder testBuilder04 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
//                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.STUDY_ID, input).boost((float) 1.50))
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.STUDY_NAME, input))
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.STUDY_TYPE, input))
                );


        SearchSourceBuilder testBuilder05 = new SearchSourceBuilder()
                .size(size)
                .from(offset)
//                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.FILE_ID_GS, input).boost((float) 1.50))
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.FILE_NAME, "*" + input + "*" ))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_FORMAT_GS, input))
                );

        SearchSourceBuilder testBuilder06 = new SearchSourceBuilder()
                .size(ES_UNITS.MAX_SIZE)
                .from(0)
//                .sort(Const.BENTO_FIELDS.PROGRAM_KW + ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(
                        addConditionalQuery(
                                new BoolQueryBuilder()
                                .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.VALUE, input))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_NAME + ES_UNITS.KEYWORD, input))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_TYPE + ES_UNITS.KEYWORD, input))
                                .should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION, input))
                                .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.NODE_NAME + ES_UNITS.KEYWORD, input)),
                                // Set Conditional Bool Query
                                QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_REQUIRED,StrUtil.getBoolText(input)))
                ).highlighter(
                        new HighlightBuilder()
                                // Index model_properties
                                .field(Const.BENTO_FIELDS.PROPERTY_NAME)
                                .field(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION)
                                .field(Const.BENTO_FIELDS.PROPERTY_TYPE)
                                .field(Const.BENTO_FIELDS.PROPERTY_REQUIRED)
                                // Index model_values
                                .field(Const.BENTO_FIELDS.VALUE)
                                // Index model_nodes
                                .field(Const.BENTO_FIELDS.NODE_NAME)
                                .preTags("")
                                .postTags("")
                                .fragmentSize(1)
                );


        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(testBuilder01))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE,
                                Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID,
                                Const.BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID,
                                BENTO_FIELDS.PROGRAM, Const.BENTO_FIELDS.PROGRAM,
                                Const.BENTO_FIELDS.STUDY_ACRONYM, Const.BENTO_FIELDS.STUDY_ACRONYM,
                                Const.BENTO_FIELDS.DIAGNOSES, Const.BENTO_FIELDS.DIAGNOSES,
                                Const.BENTO_FIELDS.AGE_AT_INDEX, Const.BENTO_FIELDS.AGE_AT_INDEX
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST02")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SAMPLES)
                                .source(testBuilder02))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE,
                                Const.BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID,
                                Const.BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID,
                                BENTO_FIELDS.SAMPLE_ID, Const.BENTO_FIELDS.SAMPLE_ID,
                                Const.BENTO_FIELDS.DIAGNOSES, Const.BENTO_FIELDS.DIAGNOSES,
                                Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE, Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE,
                                Const.BENTO_FIELDS.TISSUE_TYPE, Const.BENTO_FIELDS.TISSUE_TYPE
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST03")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.PROGRAMS)
                                .source(testBuilder03))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.PROGRAM_CODE, Const.BENTO_FIELDS.PROGRAM_CODE,
                                BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID,
                                BENTO_FIELDS.PROGRAM_NAME, Const.BENTO_FIELDS.PROGRAM_NAME
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST04")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.STUDIES)
                                .source(testBuilder04))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID,
                                BENTO_FIELDS.STUDY_ID, Const.BENTO_FIELDS.STUDY_ID,
                                BENTO_FIELDS.STUDY_TYPE, Const.BENTO_FIELDS.STUDY_TYPE,
                                BENTO_FIELDS.STUDY_CODE, Const.BENTO_FIELDS.STUDY_CODE,
                                BENTO_FIELDS.STUDY_NAME, Const.BENTO_FIELDS.STUDY_NAME
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST05")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES_TEST)
                                .source(testBuilder05))
                        .typeMapper(typeMapper.getDefaultReturnTypes(Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.PROGRAM_ID, Const.BENTO_FIELDS.PROGRAM_ID,
                                BENTO_FIELDS.SUBJECT_ID, Const.BENTO_FIELDS.SUBJECT_ID,
                                BENTO_FIELDS.SAMPLE_ID, Const.BENTO_FIELDS.SAMPLE_ID,
                                BENTO_FIELDS.FILE_NAME, Const.BENTO_FIELDS.FILE_NAME,
                                BENTO_FIELDS.FILE_FORMAT, Const.BENTO_FIELDS.FILE_FORMAT,
                                BENTO_FIELDS.FILE_ID, Const.BENTO_FIELDS.FILE_ID
                        ))).build(),
                MultipleRequests.builder()
                        .name("TEST06")
                        .request(new SearchRequest()
                                .indices(new String[]{BENTO_INDEX.MODEL_PROPERTIES, BENTO_INDEX.MODEL_VALUES, BENTO_INDEX.MODEL_NODES})
                                .source(testBuilder06))
                        .typeMapper(typeMapper.getMapWithHighlightedFields(Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE,
                                BENTO_FIELDS.NODE_NAME, BENTO_FIELDS.NODE_NAME,
                                BENTO_FIELDS.PROPERTY_NAME, Const.BENTO_FIELDS.PROPERTY_NAME,
                                BENTO_FIELDS.PROPERTY_DESCRIPTION, Const.BENTO_FIELDS.PROPERTY_DESCRIPTION,
                                BENTO_FIELDS.PROPERTY_TYPE, Const.BENTO_FIELDS.PROPERTY_TYPE,
                                BENTO_FIELDS.PROPERTY_REQUIRED, Const.BENTO_FIELDS.PROPERTY_REQUIRED,
                                BENTO_FIELDS.VALUE, Const.BENTO_FIELDS.VALUE
                        ))).build()
        );

        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) multiResult.get("TEST01");
        result.put("subjects", test01Result.getSearchHits());
        result.put("subject_count", test01Result.getTotalHits());

        QueryResult test02Result = (QueryResult) multiResult.get("TEST02");
        result.put("samples", test02Result.getSearchHits());
        result.put("sample_count", test02Result.getTotalHits());

        QueryResult test03Result = (QueryResult) multiResult.get("TEST03");
        result.put("programs", test03Result.getSearchHits());
        result.put("program_count", test03Result.getTotalHits());

        QueryResult test04Result = (QueryResult) multiResult.get("TEST04");
        result.put("studies", test04Result.getSearchHits());
        result.put("study_count", test04Result.getTotalHits());

        QueryResult test05Result = (QueryResult) multiResult.get("TEST05");
        result.put("files", test05Result.getSearchHits());
        result.put("file_count", test05Result.getTotalHits());

        QueryResult test06Result = (QueryResult) multiResult.get("TEST06");
        result.put("model", test06Result.getSearchHits());
        result.put("model_count", test06Result.getTotalHits());

        List<Map<String, Object>> about_results = searchAboutPageTest(input);
        int about_count = about_results.size();
        result.put("about_count", about_count);
        result.put("about_page", paginate(about_results, size, offset));
        // TODO
        for (String category: combinedCategories) {
            List<Object> pagedCategory = paginate((List)result.get(category), size, offset);
            result.put(category, pagedCategory);
        }

        return result;
    }

    // Add Conditional Query
    private BoolQueryBuilder addConditionalQuery(BoolQueryBuilder builder, QueryBuilder... query) {
        List<QueryBuilder> builders = Arrays.asList(query);
        builders.forEach(q->{
            if (q.getName().equals("match")) {
                MatchQueryBuilder matchQuery = getQuery(q);
                if (!matchQuery.value().equals("")) builder.should(q);
            } else if (q.getName().equals("term")) {
                TermQueryBuilder termQuery = getQuery(q);
                if (!termQuery.value().equals("")) builder.should(q);
            }
        });
        return builder;
    }

    @SuppressWarnings("unchecked")
    private <T> T getQuery(QueryBuilder q) {
        String queryType = q.getName();
        return (T) q.queryName(queryType);
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

    private List<Map<String, Object>> searchAboutPageTest(String input) throws IOException {

        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        bool.should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.CONTENT_PARAGRAPH, input));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(Const.BENTO_INDEX.ABOUT);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(Const.BENTO_FIELDS.CONTENT_PARAGRAPH);
        highlightBuilder.preTags(ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        highlightBuilder.postTags(ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        builder.highlighter(highlightBuilder);
        request.source(builder);

        Map<String, String> returnTypes = new HashMap<>();
        returnTypes.put(Const.BENTO_FIELDS.PAGE, Const.BENTO_FIELDS.PAGE);
        returnTypes.put(Const.BENTO_FIELDS.TITLE, Const.BENTO_FIELDS.TITLE);
        returnTypes.put(Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.TYPE);
        returnTypes.put(Const.BENTO_FIELDS.TEXT, Const.BENTO_FIELDS.TEXT);

        List<Map<String, Object>> result = esService.elasticSend(returnTypes, request,
                typeMapper.getHighLightFragments(Const.BENTO_FIELDS.CONTENT_PARAGRAPH,
                        (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.ABOUT,
                                Const.BENTO_FIELDS.PAGE, source.get(Const.BENTO_FIELDS.PAGE),
                                Const.BENTO_FIELDS.TITLE,source.get(Const.BENTO_FIELDS.TITLE),
                                Const.BENTO_FIELDS.TEXT, text)));

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
                "size", ESServiceImpl.MAX_ES_SIZE
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
//            searchClauses.add(Map.of("match_phrase_prefix", Map.of(searchFieldName, input)));
            searchClauses.add(Map.of("match", Map.of(searchFieldName, input)));
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

    private List<Map<String, Object>> findSubjectIdsInListTest(QueryParam param) throws IOException {
        // Set Filter
        SearchSourceBuilder builder = esService.createSourceBuilder(param);
        builder.size(ES_UNITS.MAX_SIZE);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.SUBJECTS);
        request.source(builder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

    private List<Map<String, Object>> findSubjectIdsInList(Map<String, Object> params) throws IOException {
        final String[][] properties = new String[][]{
                new String[]{"subject_id", "subject_id"},
                new String[]{"program_id", "program_id"}
        };

        Map<String, Object> query = esService.buildListQuery(params, Set.of(), true);
        Request request = new Request("GET", SUBJECT_IDS_END_POINT);

        return esService.collectPage(request, query, properties, ESServiceImpl.MAX_ES_SIZE, 0);
    }

    private List<Map<String, Object>> filesInListTest(QueryParam param) throws IOException {
        SearchSourceBuilder searchSourceBuilder = esService.createPageSourceBuilder(param, BENTO_FIELDS.FILE_NAME);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES_TEST);
        request.source(searchSourceBuilder);
        List<Map<String, Object>> result = esService.elasticSend(param.getReturnTypes(), request, typeMapper.getDefault());
        return result;
    }

    private List<Map<String, Object>> filesInList(Map<String, Object> params) throws IOException {
        final String[][] properties = new String[][]{
                new String[]{"study_code", "study_acronym"},
                new String[]{"subject_id", "subject_ids"},
                new String[]{"file_name", "file_names"},
                new String[]{"file_type", "file_type"},
                new String[]{"association", "association"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"file_id", "file_ids"},
                new String[]{"md5sum", "md5sum"}
        };

        String defaultSort = "file_name"; // Default sort order

        Map<String, String> mapping = Map.ofEntries(
                Map.entry("study_code", "study_acronym"),
                Map.entry("subject_id", "subject_id_num"),
                Map.entry("file_name", "file_names"),
                Map.entry("file_type", "file_type"),
                Map.entry("association", "association"),
                Map.entry("file_description", "file_description"),
                Map.entry("file_format", "file_format"),
                Map.entry("file_size", "file_size"),
                Map.entry("file_id", "file_id_num"),
                Map.entry("md5sum", "md5sum")
        );

        Map<String, Object> query = esService.buildListQuery(params, Set.of(PAGE_SIZE, OFFSET, ORDER_BY, SORT_DIRECTION));
        String order_by = (String)params.get(ORDER_BY);
        String direction = ((String)params.get(SORT_DIRECTION)).toLowerCase();
        query.put("sort", mapSortOrder(order_by, direction, defaultSort, mapping));
        int pageSize = (int) params.get(PAGE_SIZE);
        int offset = (int) params.get(OFFSET);
        Request request = new Request("GET", FILES_END_POINT);
        List<Map<String, Object>> result = esService.collectPage(request, query, properties, pageSize, offset);
        return result;
    }

    private List<String> fileIDsFromListTest(QueryParam param) throws IOException {
        SearchSourceBuilder builder = esService.createSourceBuilder(param);
        builder.size(ES_UNITS.MAX_SIZE);
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(BENTO_INDEX.FILES_TEST);
        request.source(builder);
        List<String>  result = esService.elasticSend(null, request, typeMapper.getStrList(BENTO_FIELDS.FILE_ID));
        return result;
    }

    private List<String> fileIDsFromList(Map<String, Object> params) throws IOException {
        return collectFieldFromList(params, "file_ids", FILES_END_POINT);
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
    // TODO
    private Map<String, Object> multiSearchTest(QueryParam param) throws IOException {

        Map<String, Object> args = param.getArgs();
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_PROGRAMS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.PROGRAMS)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_STUDIES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.STUDIES)
                                .source(new SearchSourceBuilder().size(0)))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_SUBJECTS)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SUBJECTS)
                                .source(new SearchCountFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_SAMPLES)
                        .request(new SearchRequest()
                                .indices(Const.BENTO_INDEX.SAMPLES)
                                .source(new SearchCountFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_LAB_PROCEDURES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new SearchCountFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_FILES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(new SearchCountFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getIntTotal()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_PROGRAM)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.PROGRAM + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PROGRAM)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.PROGRAM + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_STUDY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.STUDIES + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_STUDY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.STUDIES + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_DIAGNOSES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.DIAGNOSES + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_DIAGNOSIS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.DIAGNOSES + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_RECURRENCE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.RC_SCORES + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_RECURRENCE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.RC_SCORES + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TUMOR_SIZE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.TUMOR_SIZES + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TUMOR_SIZE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.TUMOR_SIZES + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TUMOR_GRADE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.TUMOR_GRADES + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TUMOR_GRADE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.TUMOR_GRADES + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_ER_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.ER_STATUS + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_ER_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.ER_STATUS + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_PR_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.PR_STATUS + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PR_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.PR_STATUS + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_CHEMO)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.CHEMO_REGIMEN + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_PR_CHEMMO)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.CHEMO_REGIMEN + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_ENDO_THERAPY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.ENDO_THERAPIES + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_ENDO_THERAPY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.ENDO_THERAPIES + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_MENO_THERAPY)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.MENO_STATUS + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_MENO_STATUS)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.MENO_STATUS + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TISSUE_TYPE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.TISSUE_TYPE + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TISSUE_TYPE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.TISSUE_TYPE + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),



                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_TISSUE_COMPOSITION)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                                FilterParam.builder()
                                                        .args(args)
                                                        .selectedField(BENTO_FIELDS.COMPOSITION + ES_UNITS.KEYWORD)
                                                        .build())
                                                .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_TISSUE_COMPOSITION)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                                FilterParam.builder()
                                                        .args(args)
                                                        .selectedField(BENTO_FIELDS.COMPOSITION + ES_UNITS.KEYWORD)
                                                        .isExcludeFilter(true)
                                                        .build())
                                                .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_FILE_ASSOCI)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.ASSOCIATION + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_FILE_ASSOCIATION)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.ASSOCIATION + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),



                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_FILE_TYPE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.FILE_TYPE + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_FILE_TYPE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.FILE_TYPE + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),


                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.NO_OF_ARMS_PROGRAM)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new SubAggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.PROGRAM + ES_UNITS.KEYWORD)
                                                .subAggSelectedField(BENTO_FIELDS.STUDY_ACRONYM + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getArmProgram()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.SUBJECT_COUNT_LAB_PROCEDURES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.LAB_PROCEDURES + ES_UNITS.KEYWORD)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),

                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_LAB_PROCEDURES)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.LAB_PROCEDURES + ES_UNITS.KEYWORD)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getAggregate()).build(),
                // Range Query
                MultipleRequests.builder()
                        .name(Bento_GraphQL_KEYS.FILTER_SUBJECT_CNT_BY_AGE)
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new RangeFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(BENTO_FIELDS.AGE_AT_INDEX)
                                                .isExcludeFilter(true)
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(typeMapper.getRange()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        return result;
    }

    static class Bento_GraphQL_KEYS {
        static final String NO_OF_PROGRAMS = "numberOfPrograms";
        static final String NO_OF_STUDIES = "numberOfStudies";
        static final String NO_OF_LAB_PROCEDURES = "numberOfLabProcedures";
        static final String NO_OF_SUBJECTS = "numberOfSubjects";
        static final String NO_OF_SAMPLES = "numberOfSamples";
        static final String NO_OF_FILES = "numberOfFiles";
        static final String NO_OF_ARMS_PROGRAM = "armsByPrograms";
        static final String SUBJECT_COUNT_PROGRAM = "subjectCountByProgram";
        static final String SUBJECT_COUNT_STUDY = "subjectCountByStudy";
        static final String SUBJECT_COUNT_DIAGNOSES = "subjectCountByDiagnoses";
        static final String SUBJECT_COUNT_RECURRENCE = "subjectCountByRecurrenceScore";
        static final String SUBJECT_COUNT_TUMOR_SIZE = "subjectCountByTumorSize";
        static final String SUBJECT_COUNT_TUMOR_GRADE = "subjectCountByTumorGrade";
        static final String SUBJECT_COUNT_ER_STATUS = "subjectCountByErStatus";
        static final String SUBJECT_COUNT_PR_STATUS = "subjectCountByPrStatus";
        static final String SUBJECT_COUNT_CHEMO = "subjectCountByChemotherapyRegimen";
        static final String SUBJECT_COUNT_ENDO_THERAPY = "subjectCountByEndocrineTherapy";
        static final String SUBJECT_COUNT_MENO_THERAPY = "subjectCountByMenopauseStatus";
        static final String SUBJECT_COUNT_TISSUE_TYPE = "subjectCountByTissueType";
        static final String SUBJECT_COUNT_TISSUE_COMPOSITION = "subjectCountByTissueComposition";
        static final String SUBJECT_COUNT_FILE_ASSOCI = "subjectCountByFileAssociation";

        static final String SUBJECT_COUNT_FILE_TYPE = "subjectCountByFileType";
        static final String FILTER_SUBJECT_CNT_PROGRAM = "filterSubjectCountByProgram";
        static final String FILTER_SUBJECT_CNT_STUDY = "filterSubjectCountByStudy";
        static final String FILTER_SUBJECT_CNT_DIAGNOSIS = "filterSubjectCountByDiagnoses";
        static final String FILTER_SUBJECT_CNT_RECURRENCE = "filterSubjectCountByRecurrenceScore";
        static final String FILTER_SUBJECT_CNT_TUMOR_SIZE = "filterSubjectCountByTumorSize";
        static final String FILTER_SUBJECT_CNT_TUMOR_GRADE = "filterSubjectCountByTumorGrade";
        static final String FILTER_SUBJECT_CNT_ER_STATUS = "filterSubjectCountByErStatus";
        static final String FILTER_SUBJECT_CNT_PR_STATUS = "filterSubjectCountByPrStatus";
        static final String FILTER_SUBJECT_CNT_PR_CHEMMO = "filterSubjectCountByChemotherapyRegimen";
        static final String FILTER_SUBJECT_CNT_ENDO_THERAPY = "filterSubjectCountByEndocrineTherapy";
        static final String FILTER_SUBJECT_CNT_MENO_STATUS = "filterSubjectCountByMenopauseStatus";
        static final String FILTER_SUBJECT_CNT_TISSUE_TYPE = "filterSubjectCountByTissueType";
        static final String FILTER_SUBJECT_CNT_TISSUE_COMPOSITION = "filterSubjectCountByTissueComposition";
        static final String FILTER_SUBJECT_CNT_FILE_ASSOCIATION = "filterSubjectCountByFileAssociation";
        static final String FILTER_SUBJECT_CNT_FILE_TYPE = "filterSubjectCountByFileType";
        static final String FILTER_SUBJECT_CNT_BY_AGE = "filterSubjectCountByAge";

        static final String SUBJECT_COUNT_LAB_PROCEDURES = "subjectCountByLabProcedures";
        static final String FILTER_SUBJECT_CNT_LAB_PROCEDURES = "filterSubjectCountByLabProcedures";

        static final String PROGRAM_COUNT = "program_count";
        static final String PROGRAMS = "programs";
        static final String STUDY_COUNT = "study_count";
        static final String STUDIES = "studies";
        static final String SUBJECT_COUNT = "subject_count";
        static final String SUBJECTS = "subjects";
        static final String SAMPLE_COUNT = "sample_count";
        static final String SAMPLES = "samples";
        static final String FILE_COUNT = "file_count";
        static final String FILES = "files";
        static final String ABOUT_COUNT = "about_count";
        static final String ABOUT_PAGE = "about_page";
        static final String MODEL_COUNT = "model_count";
        static final String MODEL = "model";

    }
}
