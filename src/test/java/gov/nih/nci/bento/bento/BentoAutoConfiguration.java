package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.GroupQuery;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.classes.SingleQuery;
import gov.nih.nci.bento.search.query.filter.AggregationFilter;
import gov.nih.nci.bento.search.query.filter.TableFilter;
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
import java.util.HashMap;
import java.util.Map;

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
        Map<String, DataFetcher> map = new HashMap<>();
        singleQuery.getQuery().forEach(q->{
            map.put(q.getName(), env -> getXMLQuery(esService.CreateQueryParam(env), q));
        });
    }

    private Object getXMLQuery(QueryParam param, SingleQuery.Query query) throws IOException {
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(query.getIndex());
        request.source(getSourceBuilder(param, query));
        return esService.elasticSend(request, getTypeMapper(param, query));
    }

    @Test
    public void groupQueryYaml_Test() throws IOException {
        Yaml yaml = new Yaml(new Constructor(GroupQuery.class));
        GroupQuery test = yaml.load(new ClassPathResource("group_query.yml").getInputStream());

        System.out.println(test);
    }


    private SearchSourceBuilder getSourceBuilder(QueryParam param, SingleQuery.Query query) {
        // Set Arguments
        SingleQuery.FilterType filterType = query.getFilterType();
        if (filterType.getType().equals("aggregation")) {
            return new AggregationFilter(
                    FilterParam.builder()
                            .args(param.getArgs())
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
        }
        throw new IllegalArgumentException();
    }


    private TypeMapper getTypeMapper(QueryParam param, SingleQuery.Query query) {
        // Set Result Type
        if (query.getResultType().equals("default")) {
            return typeMapper.getDefault(param.getReturnTypes());
        } else if (query.getResultType().equals("agg")) {
            return typeMapper.getAggregate();
        } else if (query.getResultType().equals("count")) {
            return typeMapper.getIntTotal();
        } else if (query.getResultType().equals("agg_count")) {
            return typeMapper.getAggregateTotalCnt();
        }
        return typeMapper.getDefault(param.getReturnTypes());
    }

}
