package gov.nih.nci.bento.bento;


import gov.nih.nci.bento.constants.Const.BENTO_INDEX;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.search.MultipleRequests;
import gov.nih.nci.bento.model.search.filter.DefaultFilter;
import gov.nih.nci.bento.model.search.filter.FilterParam;
import gov.nih.nci.bento.model.search.filter.TableFilter;
import gov.nih.nci.bento.model.search.filter.TableParam;
import gov.nih.nci.bento.model.search.mapper.TypeMapperImpl;
import gov.nih.nci.bento.model.search.query.QueryResult;
import gov.nih.nci.bento.service.ESService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@RunWith( SpringRunner.class )
@SpringBootTest
public class BentoFilterTest {

    @Autowired
    ESService esService;

    @Autowired
    TypeMapperImpl typeMapper;

    @Autowired
    ConfigurationDAO config;

    @Test
    public void subjectsInList_Test() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("subject_id", List.of("BENTO-CASE-7356713"));

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SUBJECTS)
                                .source(new DefaultFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField("subject_id")
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(
                                typeMapper.getQueryResult(
                                        new HashSet<>() {{
                                            add("program");
                                            add("program_id");
                                            add("study_acronym");
                                            add("subject_id");
                                            add("diagnosis");
                                            add("recurrence_score");
                                            add("tumor_size");
                                            add("er_status");
                                            add("pr_status");
                                            add("age_at_index");
                                            add("survival_time");
                                            add("survival_time_unit");
                                            }
                                        }))
                        .build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) result.get("TEST01");
        assertThat(test01Result.getTotalHits(), greaterThan(0));
    }

    @Test
    public void filesOfSubjects_Test() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("subject_id", List.of("BENTO-CASE-7356713"));

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(new DefaultFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField("subject_id")
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(
                                typeMapper.getQueryResult(
                                        new HashSet<>() {
                                            {
                                                add("subject_id");
                                                add("file_name");
                                                add("file_type");
                                                add("association");
                                                add("file_description");
                                                add("file_format");
                                                add("file_size");
                                                add("file_id");
                                                add("md5sum");
                                            }
                                        }))
                        .build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) result.get("TEST01");
        assertThat(test01Result.getTotalHits(), greaterThan(0));
    }

    @Test
    public void samplesForSubjectId_Test() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("subject_id", List.of("BENTO-CASE-7356713"));

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.SAMPLES)
                                .source(new DefaultFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField("subject_id")
                                                .build())
                                        .getSourceFilter()
                                ))
                        .typeMapper(
                                typeMapper.getQueryResult(
                                        new HashSet<>() {{
                                            add("program");
                                            add("program_id");
                                            add("arm");
                                            add("subject_id");
                                            add("sample_id");
                                            add("diagnosis");
                                            add("tissue_type");
                                            add("tissue_composition");
                                            add("sample_anatomic_site");
                                            add("sample_procurement_method");
                                            add("platform");
                                            add("files");
                                        }
                                        }))
                        .build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) result.get("TEST01");
        assertThat(test01Result.getTotalHits(), greaterThan(0));
    }

//
//    public QueryParam CreateQueryParam(DataFetchingEnvironment env) {
//        return QueryParam.builder()
//                .args(env.getArguments())
//                .outputType(env.getFieldType())
//                .build();
//    }


    @Test
    public void fileIdsFromFileName_Test() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("file_name", "1025_OncotypeDXqRTPCR.txt");
//        args.put("offset", 0);
//        args.put("order_by", "file_name");
//        args.put("sort_direction", "desc");
//        QueryParam queryParam = CreateQueryParam(env);
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(BENTO_INDEX.FILES)
                                .source(
                                        new TableFilter(FilterParam.builder()
                                                .args(args)
                                                .tableParam(
                                                        TableParam.builder()
                                                                .pageSize(10).offSet(0).orderBy("file_name").sortDirection(SortOrder.ASC)
                                                                .build()
                                                )
//                                                .customOrderBy(getIntCustomOrderBy_Test(param, query))
//                                                .defaultSortField(filterType.getDefaultSortField())
                                                .build())
                                        .getSourceFilter()
                                )
                        )
                        .typeMapper(
                                typeMapper.getQueryResult(
                                        new HashSet<>() {{
                                            add("study_code");
                                            add("subject_id");
                                            add("file_name");
                                            add("file_type");
                                            add("association");
                                            add("file_description");
                                            add("file_format");
                                            add("file_size");
                                            add("file_id");
                                            add("md5sum");
                                        }
                                        }))
                        .build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) result.get("TEST01");
        assertThat(test01Result.getTotalHits(), greaterThan(0));
    }




    @Test
    public void fileIdsFromFileNameDesc_Test() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("file_name", List.of("1025_OncotypeDXqRTPCR.txt"));
//        args.put("offset", 0);
//        args.put("order_by", "file_name");
//        args.put("sort_direction", "desc");
//        QueryParam queryParam = CreateQueryParam(env);


        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                        .indices(BENTO_INDEX.FILES)
                                        .source(
                                                new TableFilter(FilterParam.builder()
                                                        .args(args)
                                                        .tableParam(
                                                            TableParam.builder()
                                                                    .pageSize(10).offSet(0).orderBy("file_name").sortDirection(SortOrder.DESC)
                                                                    .build()
                                                        )
//                                                .customOrderBy(getIntCustomOrderBy_Test(param, query))
//                                                .defaultSortField(filterType.getDefaultSortField())
                                                        .build())
                                                        .getSourceFilter()
                                        )
                        )
                        .typeMapper(
                                typeMapper.getQueryResult(
                                        new HashSet<>() {{
                                            add("study_code");
                                            add("subject_id");
                                            add("file_name");
                                            add("file_type");
                                            add("association");
                                            add("file_description");
                                            add("file_format");
                                            add("file_size");
                                            add("file_id");
                                            add("md5sum");
                                        }
                                        }))
                        .build());

        Map<String, Object> result = esService.elasticMultiSend(requests);
        QueryResult test01Result = (QueryResult) result.get("TEST01");
        assertThat(test01Result.getTotalHits(), greaterThan(0));
    }


}

