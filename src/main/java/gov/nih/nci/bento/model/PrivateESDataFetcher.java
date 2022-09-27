package gov.nih.nci.bento.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.model.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class PrivateESDataFetcher extends AbstractESDataFetcher {
    private static final Logger logger = LogManager.getLogger(PrivateESDataFetcher.class);
    private final YamlQueryFactory yamlQueryFactory;

    public PrivateESDataFetcher(ESService esService) {
        super(esService);
        yamlQueryFactory = new YamlQueryFactory(esService);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetchers(yamlQueryFactory.createYamlQueries())
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
                        .dataFetcher("filesInList", env -> {
                            Map<String, Object> args = env.getArguments();
                            return filesInList(args);
                        })
                        .dataFetcher("idsLists", env -> idsLists())
                        .dataFetcher("programInfo", env -> programInfo())
                        .dataFetcher("programDetail", env -> {
                            Map<String, Object> args = env.getArguments();
                            return programDetail(args);
                        })
                        .dataFetcher("subjectDetail", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectDetail(args);
                        })
                        .dataFetcher("armDetail", env -> {
                            Map<String, Object> args = env.getArguments();
                            return armDetail(args);
                        })
                )
                .build();
    }

    private Map<String, Object> programDetail(Map<String, Object> params) throws IOException {
        // Get filter parameter as a String
        final String PROGRAM_ID = (String) params.get("program_id");
        // Declare properties mappings
        final String[][] PROGRAMS_PROPERTIES = new String[][]{
                new String[]{"program_acronym", "program_code_kw"},
                new String[]{"program_id", "program_id_kw"},
                new String[]{"program_name", "program_name_kw"},
                new String[]{"program_full_description", "program_full_description"},
                new String[]{"institution_name", "institution_name"},
                new String[]{"program_external_url", "program_external_url"},
                new String[]{"num_subjects", "num_subjects"},
                new String[]{"num_files", "num_files"},
                new String[]{"num_samples", "num_samples"},
                new String[]{"num_lab_procedures", "num_lab_procedures"}
        };
        final String[][] STUDIES_PROPERTIES = new String[][]{
                new String[]{"study_acronym", "study_code"},
                new String[]{"study_name", "study_name"},
                new String[]{"study_full_description", "study_full_description"},
                new String[]{"study_type", "study_type"},
                new String[]{"study_info", "study_info"},
                new String[]{"num_subjects", "num_subjects"}
        };
        // Get program information use it to initialize the result
        List<Map<String, Object>> programsResultList = esService.collectPage(Map.of("program_id_kw", PROGRAM_ID),
                PROGRAMS_END_POINT, PROGRAMS_PROPERTIES);
        if (programsResultList.isEmpty()){
            return null;
        }
        Map<String, Object> programsResult = programsResultList.get(0);
        // Add study information array to the result as the property "studies"
        List<Map<String, Object>> studiesResult = esService.collectPage(Map.of("program_id_kw", PROGRAM_ID),
                STUDIES_END_POINT, STUDIES_PROPERTIES);
        programsResult.put("studies", studiesResult);
        // Add diagnoses aggregations to the result as the property "diagnoses"
        Map<String, Object> diagnosesParams = Map.of("program_id", PROGRAM_ID);
        List<Map<String, Object>> diagnosesGroupCounts = esService.getFilteredGroupCount(diagnosesParams,
                SUBJECTS_END_POINT, "diagnoses");
        programsResult.put("diagnoses", diagnosesGroupCounts);
        // Add list of unique diagnoses from aggregations to the result as the property "disease_subtypes"
        HashSet<String> diseaseSubtypes = new HashSet<>();
        diagnosesGroupCounts.forEach((Map<String, Object> groupCount) -> {
            diseaseSubtypes.add((String) groupCount.get("group"));
        });
        programsResult.put("disease_subtypes", diseaseSubtypes);
        // Return result
        return programsResult;
    }

    private Map<String, Object> subjectDetail(Map<String, Object> params) throws IOException {
        final String SUBJECT_ID = (String) params.get("subject_id");
        final String[][] SUBJECT_PROPERTIES = new String[][]{
                new String[]{"subject_id", "subject_ids"},
                new String[]{"program_acronym", "programs"},
                new String[]{"program_id", "program_id"},
                new String[]{"study_acronym", "study_acronym"},
                new String[]{"study_name", "study_name"},
                new String[]{"gender", "gender"},
                new String[]{"race", "race"},
                new String[]{"ethnicity", "ethnicity"},
                new String[]{"age_at_index", "age_at_index"},
                new String[]{"menopause_status", "meno_status"},
                new String[]{"vital_status", "vital_status"},
                new String[]{"cause_of_death", "cause_of_death"},
                new String[]{"disease_type", "disease_type"},
                new String[]{"disease_subtype", "diagnoses"},
                new String[]{"tumor_grade", "tumor_grades"},
                new String[]{"tumor_largest_dimension_diameter", "tumor_largest_dimension_diameter"},
                new String[]{"er_status", "er_status"},
                new String[]{"pr_status", "pr_status"},
                new String[]{"nuclear_grade", "nuclear_grade"},
                new String[]{"recurrence_score", "recurrence_score"},
                new String[]{"primary_surgical_procedure", "primary_surgical_procedure"},
                new String[]{"chemotherapy_regimen_group", "chemotherapy_regimen_group"},
                new String[]{"chemotherapy_regimen", "chemotherapy_regimen"},
                new String[]{"endocrine_therapy_type", "endocrine_therapy_type"},
                new String[]{"dfs_event_indicator", "dfs_event_indicator"},
                new String[]{"recurrence_free_indicator", "recurrence_free_indicator"},
                new String[]{"distant_recurrence_indicator", "distant_recurrence_indicator"},
                new String[]{"dfs_event_type", "dfs_event_type"},
                new String[]{"first_recurrence_type", "first_recurrence_type"},
                new String[]{"days_to_progression", "days_to_progression"},
                new String[]{"days_to_recurrence", "days_to_recurrence"},
                new String[]{"test_name", "test_name"},
                new String[]{"num_samples", "num_samples"},
                new String[]{"num_lab_procedures", "num_lab_procedures"}
        };
        final String[][] FILES_PROPERTIES = new String[][]{
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
        final String[][] SAMPLES_PROPERTIES = new String[][]{
                new String[]{"sample_id", "sample_ids"},
                new String[]{"sample_anatomic_site", "sample_anatomic_site"},
                new String[]{"composition", "composition"},
                new String[]{"method_of_sample_procurement", "sample_procurement_method"},
                new String[]{"tissue_type", "tissue_type"},
                new String[]{"sample_type", "sample_type"}
        };
        Map<String, Object> filterParam = Map.of("subject_ids", SUBJECT_ID);
        // Get subject details and initialize result
        List<Map<String, Object>> subjectsResultList = esService.collectPage(filterParam, SUBJECTS_END_POINT,
                SUBJECT_PROPERTIES);
        if (subjectsResultList.isEmpty()){
            return null;
        }
        Map<String, Object> subjectsResult = subjectsResultList.get(0);
        // Add subject files array to the result
        List<Map<String, Object>> filesResultList = esService.collectPage(filterParam, FILES_END_POINT,
                FILES_PROPERTIES);
        subjectsResult.put("files", filesResultList);
        // Add subject samples array to the result
        List<Map<String, Object>> samplesResultList = esService.collectPage(filterParam, SAMPLES_END_POINT,
                SAMPLES_PROPERTIES);
        subjectsResult.put("samples", samplesResultList);
        return subjectsResult;
    }


    private Map<String, Object> armDetail(Map<String, Object> params) throws IOException {
        final String STUDY_ACRONYM = (String) params.get("study_acronym");
        final String[][] STUDY_PROPERTIES = new String[][]{
                new String[]{"study_acronym", "study_code"},
                new String[]{"study_name", "study_name"},
                new String[]{"study_type", "study_type"},
                new String[]{"study_full_description", "study_full_description"},
                new String[]{"study_info", "study_info"},
                new String[]{"num_subjects", "num_subjects"},
                new String[]{"num_files", "num_files"},
                new String[]{"num_samples", "num_samples"},
                new String[]{"num_lab_procedures", "num_laboratory_procedures"},
        };
        final String[][] FILES_PROPERTIES = new String[][]{
                new String[]{"file_name", "file_names"},
                new String[]{"file_type", "file_type"},
                new String[]{"association", "association"},
                new String[]{"file_description", "file_description"},
                new String[]{"file_format", "file_format"},
                new String[]{"file_size", "file_size"},
                new String[]{"file_id", "file_ids"},
                new String[]{"md5sum", "md5sum"}
        };
        //Get arm (study) details and initialize result
        List<Map<String, Object>> studyResultList = esService.collectPage(Map.of("study_code_kw", STUDY_ACRONYM),
                STUDIES_END_POINT, STUDY_PROPERTIES);
        if (studyResultList.isEmpty()){
            return null;
        }
        Map<String, Object> studyResult = studyResultList.get(0);
        //Add arm diagnoses group counts to result
        List<Map<String, Object>> diagnosesGroupCounts = esService.getFilteredGroupCount(Map.of("study_acronym", STUDY_ACRONYM),
                SUBJECTS_END_POINT, "diagnoses");
        studyResult.put("diagnoses", diagnosesGroupCounts);
        //Add arm files array to result
        List<Map<String, Object>> filesResultList = esService.collectPage(
                Map.of("study_acronym", STUDY_ACRONYM, "association", "study"), FILES_END_POINT,
                FILES_PROPERTIES);
        studyResult.put("files", filesResultList);
        return studyResult;
    }

    private Map<String, String[]> idsLists() throws IOException {
        /*
        Index properties map definition template:
        Map.of(
            <ES Index Endpoint>, new String[][]{
                    new String[]{<Return Label>, <ES Index property name>}
            },
        */
        Map<String, String[][]> indexProperties = Map.of(
            SUBJECT_IDS_END_POINT, new String[][]{
                    new String[]{"subjectIds", "subject_ids"}
            },
            SAMPLES_END_POINT, new String[][]{
                    new String[]{"sampleIds", "sample_ids"}
            },
            FILES_END_POINT, new String[][]{
                    new String[]{"fileIds", "file_ids"},
                    new String[]{"fileNames", "file_names"}
            }
        );
        //Generic Query
        Map<String, Object> query = esService.buildListQuery();
        //Results Map
        Map<String, String[]> results = new HashMap<>();
        //Iterate through each index properties map and make a request to each endpoint then format the results as
        // String arrays
        for (String endpoint: indexProperties.keySet()){
            Request request = new Request("GET", endpoint);
            String[][] properties = indexProperties.get(endpoint);
            List<Map<String, Object>> result = esService.collectPage(request, query, properties, ESService.MAX_ES_SIZE,
                    0);
            Map<String, List<String>> indexResults = new HashMap<>();
            Arrays.asList(properties).forEach(x -> indexResults.put(x[0], new ArrayList<>()));
            for(Map<String, Object> resultElement: result){
                for(String key: indexResults.keySet()){
                    indexResults.get(key).add((String) resultElement.get(key));
                }
            }
            for(String key: indexResults.keySet()){
                results.put(key, indexResults.get(key).toArray(new String[indexResults.size()]));
            }
        }
        return results;
    }

    private List<Map<String, Object>> programInfo() throws IOException {
        /*
            Properties definition template:
            String[][] properties = new String[][]{
                new String[]{ <Return Label>, <ES Index property name>}
            };
        */
        String[][] properties = new String[][]{
            new String[]{"program_acronym", "program_code_kw"},
            new String[]{"program_id", "program_id_kw"},
            new String[]{"program_name", "program_name_kw"},
            new String[]{"start_date", "start_date"},
            new String[]{"end_date", "end_date"},
            new String[]{"pubmed_id", "pubmed_id"},
            new String[]{"num_studies", "num_studies"},
            new String[]{"num_subjects", "num_subjects"}
        };
        //Generic Query
        Map<String, Object> query = esService.buildListQuery();
        Request request = new Request("GET", PROGRAMS_END_POINT);
        return esService.collectPage(request, query, properties, ESService.MAX_ES_SIZE, 0);
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException {
        final String AGG_NAME = "agg_name";
        final String AGG_ENDPOINT = "agg_endpoint";
        final String WIDGET_QUERY = "widgetQueryName";
        final String FILTER_COUNT_QUERY = "filterCountQueryName";
        // Query related values
        final List<Map<String, String>> TERM_AGGS = new ArrayList<>();
        TERM_AGGS.add(Map.of(
                AGG_NAME, "programs",
                WIDGET_QUERY, "subjectCountByProgram",
                FILTER_COUNT_QUERY, "filterSubjectCountByProgram",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "studies",
                WIDGET_QUERY, "subjectCountByStudy",
                FILTER_COUNT_QUERY, "filterSubjectCountByStudy",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "diagnoses",
                WIDGET_QUERY, "subjectCountByDiagnoses",
                FILTER_COUNT_QUERY, "filterSubjectCountByDiagnoses",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "rc_scores",
                WIDGET_QUERY,"subjectCountByRecurrenceScore",
                FILTER_COUNT_QUERY, "filterSubjectCountByRecurrenceScore",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "tumor_sizes",
                WIDGET_QUERY, "subjectCountByTumorSize",
                FILTER_COUNT_QUERY, "filterSubjectCountByTumorSize",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "tumor_grades",
                WIDGET_QUERY, "subjectCountByTumorGrade",
                FILTER_COUNT_QUERY, "filterSubjectCountByTumorGrade",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "er_status",
                WIDGET_QUERY, "subjectCountByErStatus",
                FILTER_COUNT_QUERY, "filterSubjectCountByErStatus",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "pr_status",
                WIDGET_QUERY, "subjectCountByPrStatus",
                FILTER_COUNT_QUERY, "filterSubjectCountByPrStatus",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "chemo_regimen",
                WIDGET_QUERY, "subjectCountByChemotherapyRegimen",
                FILTER_COUNT_QUERY, "filterSubjectCountByChemotherapyRegimen",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "endo_therapies",
                WIDGET_QUERY, "subjectCountByEndocrineTherapy",
                FILTER_COUNT_QUERY, "filterSubjectCountByEndocrineTherapy",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "meno_status",
                WIDGET_QUERY, "subjectCountByMenopauseStatus",
                FILTER_COUNT_QUERY, "filterSubjectCountByMenopauseStatus",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "tissue_type",
                WIDGET_QUERY, "subjectCountByTissueType",
                FILTER_COUNT_QUERY, "filterSubjectCountByTissueType",
                AGG_ENDPOINT, SAMPLES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "composition",
                WIDGET_QUERY, "subjectCountByTissueComposition",
                FILTER_COUNT_QUERY, "filterSubjectCountByTissueComposition",
                AGG_ENDPOINT, SAMPLES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "association",
                WIDGET_QUERY, "subjectCountByFileAssociation",
                FILTER_COUNT_QUERY, "filterSubjectCountByFileAssociation",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "file_type",
                WIDGET_QUERY, "subjectCountByFileType",
                FILTER_COUNT_QUERY, "filterSubjectCountByFileType",
                AGG_ENDPOINT, FILES_END_POINT
        ));
        TERM_AGGS.add(Map.of(
                AGG_NAME, "lab_procedures",
                WIDGET_QUERY, "subjectCountByLabProcedures",
                FILTER_COUNT_QUERY, "filterSubjectCountByLabProcedures",
                AGG_ENDPOINT, SUBJECTS_END_POINT
        ));

        List<String> agg_names = new ArrayList<>();
        for (var agg: TERM_AGGS) {
            agg_names.add(agg.get(AGG_NAME));
        }
        final String[] TERM_AGG_NAMES = agg_names.toArray(new String[TERM_AGGS.size()]);

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
        for (var agg: TERM_AGGS) {
            String field = agg.get(AGG_NAME);
            String widgetQueryName = agg.get(WIDGET_QUERY);
            String filterCountQueryName = agg.get(FILTER_COUNT_QUERY);
            String endpoint = agg.get(AGG_ENDPOINT);
            // subjectCountByXXXX
            List<Map<String, Object>> widgetData;
            if (endpoint.equals(SUBJECTS_END_POINT)) {
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
                new String[]{"diagnosis", "diagnoses"},
                new String[]{"acl", "acl"}
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
                Map.entry("diagnosis", "diagnoses"),
                Map.entry("acl", "acl")
        );

        List<Map<String, Object>> result = overview(FILES_END_POINT, params, PROPERTIES, defaultSort, mapping);
        final String ACL_KEY = "acl";
        try{
            for(Map<String, Object> resultElement: result){
                String acl = (String) resultElement.get(ACL_KEY);
                String[] acls = acl.replaceAll("\\]|\\[|'|\"", "").split(",");
                resultElement.put(ACL_KEY, acls);
            }
        }
        catch(ClassCastException | NullPointerException ex){
            logger.error("Error occurred when splitting acl into String array");
        }

        return result;
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

        return esService.collectPage(request, query, properties, pageSize, offset);
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
}
