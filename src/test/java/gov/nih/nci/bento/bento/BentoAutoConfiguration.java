package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.*;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.result.TypeMapper;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.EsSearch;
import graphql.schema.DataFetcher;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@RunWith( SpringRunner.class )
@SpringBootTest
public class BentoAutoConfiguration {

    @Autowired
    TypeMapperImpl typeMapper;

    @Autowired
    EsSearch esService;

    @Test
    public void singleQueryYaml_Test() throws IOException {
        Yaml yaml = new Yaml(new Constructor(SingleQuery.class));
        SingleQuery singleQuery = yaml.load(new ClassPathResource("single_query.yml").getInputStream());
        Map<String, DataFetcher> singleQueryMap = new HashMap<>();
        singleQuery.getQuery().forEach(q->{
            singleQueryMap.put(q.getName(), env -> getYamlQuery(esService.CreateQueryParam(env), q));
        });
        assertThat(singleQueryMap.size(), equalTo(singleQuery.getQuery().size()));
    }

    @Test
    public void groupQueryYaml_Test() throws IOException {
        Yaml yaml = new Yaml(new Constructor(GroupQuery.class));
        GroupQuery groupQuery = yaml.load(new ClassPathResource("group_query.yml").getInputStream());
        Map<String, DataFetcher> groupQueryMap = new HashMap<>();

        groupQuery.getGroups().forEach(group->{
            String queryName = group.getName();
            groupQueryMap.put(queryName, env -> createGroupQuery(group, esService.CreateQueryParam(env)));

            // Set Rest API Request
//            group.getQuery().forEach(q->{
//
//
//                SearchRequest request = new SearchRequest();
//                request.indices(q.getIndex());
//                request.source(getSourceBuilder(param, query));
//                Object obj = esService.elasticSend(request, getTypeMapper(param, query));
//
//            });




        });
        assertThat(groupQueryMap.size(), greaterThan(0));

//        Integer sum = groupQuery.getGroups().stream()
//                .mapToInt(i->i.getQuery().size())
//                .sum();
    }

    private Object createGroupQuery(GroupQuery.Group group,QueryParam param) throws IOException {
        List<MultipleRequests> requests = new ArrayList<>();
        group.getQuery().forEach(q->{
            MultipleRequests multipleRequest = MultipleRequests.builder()
                    .name(q.getName())
                    .request(new SearchRequest()
                            .indices(q.getIndex())
                            .source(getSourceBuilder(param, q)))
                    .typeMapper(getTypeMapper(param, q)).build();
            requests.add(multipleRequest);

        });
        System.out.println(requests.size());
        return esService.elasticMultiSend(requests);
    }

    private Object getYamlQuery(QueryParam param, YamlQuery query) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(query.getIndex());
        request.source(getSourceBuilder(param, query));
        return esService.elasticSend(request, getTypeMapper(param, query));
    }

    private SearchSourceBuilder getSourceBuilder(QueryParam param, YamlQuery query) {
        // Set Arguments
        YamlFilterType filterType = query.getFilterType();
        if (filterType.getType().equals("aggregation")) {
            return new AggregationFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .isExcludeFilter(filterType.isFilter())
                            .selectedField(filterType.getSelectedField())
                            .build())
                    .getSourceFilter();

        } else if (filterType.getType().equals("table")) {
            return new TableFilter(FilterParam.builder()
                    .args(param.getArgs())
                    .queryParam(param)
//                    .customOrderBy(getIntCustomOrderBy(param))
                    .defaultSortField(filterType.getSelectedField())
                    .build()).getSourceFilter();
        } else if (filterType.getType().equals("number_of_docs")) {
            return new SearchCountFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .build())
                    .getSourceFilter();
        } else if (filterType.getType().equals("range")) {
            return new RangeFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .selectedField(filterType.getSelectedField())
                            .isExcludeFilter(true)
                            .build())
                    .getSourceFilter();
        } else if (filterType.getType().equals("sub_aggregation")) {
            return new SubAggregationFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
                            .selectedField(filterType.getSelectedField())
                            .subAggSelectedField(filterType.getSubAggSelectedField())
                            .build())
                    .getSourceFilter();
        } else if (filterType.getType().equals("default")) {
            new DefaultFilter(FilterParam.builder()
                    .args(param.getArgs()).build()).getSourceFilter();
        }
        throw new IllegalArgumentException();
    }


    private TypeMapper getTypeMapper(QueryParam param, YamlQuery query) {
        // Set Result Type
        if (query.getResultType().equals("default")) {
            return typeMapper.getDefault(param.getReturnTypes());
        } else if (query.getResultType().equals("aggregation")) {
            return typeMapper.getAggregate();
        } else if (query.getResultType().equals("int_total_aggregation")) {
            return typeMapper.getAggregateTotalCnt();
        }  else if (query.getResultType().equals("range")) {
            return typeMapper.getRange();
        }  else if (query.getResultType().equals("arm_program")) {
            return typeMapper.getArmProgram();
        }  else if (query.getResultType().equals("int_total_count")) {
            return typeMapper.getIntTotal();
        }  else if (query.getResultType().equals("str_list")) {
            return typeMapper.getStrList(query.getFilterType().getSelectedField());
        }
        throw new IllegalArgumentException();
    }

}
