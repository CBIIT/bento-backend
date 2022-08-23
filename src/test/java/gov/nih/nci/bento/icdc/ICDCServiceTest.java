package gov.nih.nci.bento.icdc;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.filter.AggregationFilter;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.ESServiceImpl;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nih.nci.bento.constants.Const.ICDC_FIELDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@RunWith( SpringRunner.class )
@SpringBootTest
public class ICDCServiceTest {

    @Autowired
    ESServiceImpl esService;

    @Autowired
    TypeMapperImpl typeMapper;


    @Test
    public void sumAggregation_Test() {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(0)
                .aggregation(AggregationBuilders
                        .sum(Const.ES_PARAMS.TERMS_AGGS)
                        .field("file_size"));
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.FILES)
                                .source(builder))
                        .typeMapper(typeMapper.getSumAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        Float test01Result = (Float) result.get("TEST01");
        MatcherAssert.assertThat(test01Result, is(greaterThan(Float.valueOf(0))));
    }

    @Test
    public void breedAgg_Test() {
        // Argument Match with Fields in Elasticsearch
        Map<String, Object> args = new HashMap<>();
        args.put(ICDC_FIELDS.BREED, List.of(
                "Beagle",
                "Akita"
        ));

        // Set Argument Map
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.CASES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(ICDC_FIELDS.BREED)
                                                .build())
                                        .getSourceFilter()))
                        .typeMapper(typeMapper.getICDCAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>> test01Result = (List<Map<String, Object>>) result.get("TEST01");
        MatcherAssert.assertThat(test01Result.size(), is(greaterThan(0)));
    }


    @Test
    public void programCount_Test() {
        Map<String, Object> args = new HashMap<>();
        args.put(ICDC_FIELDS.PROGRAM, List.of("COP", "CMCP", "PCCR"));

        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name("TEST01")
                        .request(new SearchRequest()
                                .indices(Const.ICDC_INDEX.CASES)
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(args)
                                                .selectedField(ICDC_FIELDS.PROGRAM)
                                                .build())
                                        .getSourceFilter()))
                        .typeMapper(typeMapper.getICDCAggregate()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<Map<String, Object>>  test01Result = (List<Map<String, Object>>) result.get("TEST01");
        MatcherAssert.assertThat(test01Result.size(), is(greaterThan(0)));
    }

}
