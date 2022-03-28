package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.*;
import gov.nih.nci.bento.classes.yamlquery.*;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.result.TypeMapper;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.EsSearch;
import gov.nih.nci.bento.utility.StrUtil;
import graphql.schema.DataFetcher;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.util.*;

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

    @Test
    // TODO index exist test
    public void testIndexExists() {

    }

    @Test
    public void globalQueryYaml_Test() throws IOException {
        Yaml yaml = new Yaml(new Constructor(SingleQuery.class));
        SingleQuery singleQuery = yaml.load(new ClassPathResource("global_query.yml").getInputStream());
        Map<String, DataFetcher> singleQueryMap = new HashMap<>();
        singleQuery.getQuery().forEach(q->{
            singleQueryMap.put(q.getName(), env -> getYamlQuery(esService.CreateQueryParam(env), q));
        });
        assertThat(singleQueryMap.size(), equalTo(singleQuery.getQuery().size()));
    }


    private Object createGroupQuery(GroupQuery.Group group,QueryParam param) throws IOException {
        List<MultipleRequests> requests = new ArrayList<>();
        group.getQuery().forEach(q->{
            MultipleRequests multipleRequest = MultipleRequests.builder()
                    .name(q.getName())
                    .request(new SearchRequest()
//                            .indices(q.getIndex())
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
                    // TODO
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
            return new DefaultFilter(FilterParam.builder()
                    .args(param.getArgs()).build()).getSourceFilter();
        } else if (filterType.getType().equals("global")) {

            return createGlobalQuery(param,query);
        }
        throw new IllegalArgumentException();
    }


    private SearchSourceBuilder createGlobalQuery(QueryParam param, YamlQuery query) {
        TableParam tableParam = param.getTableParam();
        // Store Conditional Query
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(tableParam.getPageSize())
                .from(tableParam.getOffSet())
// TODO
//                    .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(
                        addConditionalQuery(
                                createGlobalQuerySets(param, query),
                                createGlobalConditionalQueries(param, query))
                );

        if (query.getHighlight() != null) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            HighlightQuery highlightQuery = query.getHighlight();
            // Set Multiple Highlight Fields
            highlightQuery.getFields().forEach((f)->highlightBuilder.field(f));
            highlightBuilder.preTags(highlightQuery.getPreTag() == null ? "" : highlightQuery.getPreTag());
            highlightBuilder.postTags(highlightQuery.getPostTag() == null ? "" : highlightQuery.getPreTag());
            if (highlightBuilder.fragmentSize() != null) highlightBuilder.fragmentSize(highlightQuery.getFragmentSize());
            builder.highlighter(highlightBuilder);
        }
        return builder;
    }

    private BoolQueryBuilder createGlobalQuerySets(QueryParam param, YamlQuery query) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        List<YamlGlobalFilterType.GlobalQuerySet> globalQuerySets = query.getFilterType().getQuery();
        // Add Should Query
        globalQuerySets.forEach(globalQuery -> {
            if (globalQuery.getType().equals("term")) {
                boolQueryBuilder.should(QueryBuilders.termQuery(globalQuery.getField(), param.getSearchText()));
            } else if (globalQuery.getType().equals("wildcard")) {
                boolQueryBuilder.should(QueryBuilders.wildcardQuery(globalQuery.getField(), "*" + param.getSearchText()+ "*").caseInsensitive(true));
            } else if (globalQuery.getType().equals("match")) {
                boolQueryBuilder.should(QueryBuilders.matchQuery(globalQuery.getField(), param.getSearchText()));
            } else {
                throw new IllegalArgumentException();
            }
        });
        return boolQueryBuilder;
    }

    private List<QueryBuilder> createGlobalConditionalQueries(QueryParam param, YamlQuery query) {
        List<QueryBuilder> conditionalList = new ArrayList<>();
        List<YamlGlobalFilterType.GlobalQuerySet> optionalQuerySets = query.getFilterType().getOptionalQuery();
        optionalQuerySets.forEach(option-> {
            String filterString = "";
            if (option.getOption().equals("boolean")) {
                filterString = StrUtil.getBoolText(param.getSearchText());
            } else if (option.getOption().equals("integer")) {
                filterString = StrUtil.getIntText(param.getSearchText());
            } else {
                throw new IllegalArgumentException();
            }

            if (option.getType().equals("match")) {
                conditionalList.add(QueryBuilders.matchQuery(option.getField(), filterString));
            } else if (option.getType().equals("term")) {
                conditionalList.add(QueryBuilders.termQuery(option.getField(), filterString));
            } else {
                throw new IllegalArgumentException();
            }
        });
        return conditionalList;
    }

    // Add Conditional Query
    private BoolQueryBuilder addConditionalQuery(BoolQueryBuilder builder, List<QueryBuilder> builders) {
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


    private TypeMapper getTypeMapper(QueryParam param, YamlQuery query) {
        // Set Result Type

        switch (query.getResultType()) {
            case "default":
                return typeMapper.getDefault(param.getReturnTypes());
            case "aggregation":
                return typeMapper.getAggregate();
            case "int_total_aggregation":
                return typeMapper.getAggregateTotalCnt();
            case "range":
                return typeMapper.getRange();
            case "arm_program":
                return typeMapper.getArmProgram();
            case "int_total_count":
                return typeMapper.getIntTotal();
            case "str_list":
                return typeMapper.getStrList(query.getFilterType().getSelectedField());
            // TODO
            case "global_about":
                return typeMapper.getHighLightFragments(Const.BENTO_FIELDS.CONTENT_PARAGRAPH,
                        (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.ABOUT,
                                Const.BENTO_FIELDS.PAGE, source.get(Const.BENTO_FIELDS.PAGE),
                                Const.BENTO_FIELDS.TITLE,source.get(Const.BENTO_FIELDS.TITLE),
                                Const.BENTO_FIELDS.TEXT, text));
            case "global":
                return typeMapper.getDefaultReturnTypes(param.getGlobalSearchResultTypes());
            case "global_multiples":
                return typeMapper.getMapWithHighlightedFields(param.getGlobalSearchResultTypes());
            default:
                throw new IllegalArgumentException();
        }
    }

}
