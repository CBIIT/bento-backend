package gov.nih.nci.bento.bento;

import gov.nih.nci.bento.classes.*;
import gov.nih.nci.bento.constants.Const;
import static gov.nih.nci.bento.constants.Const.YAML_QUERY;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.query.yaml.*;
import gov.nih.nci.bento.search.query.yaml.filter.YamlQuery;
import gov.nih.nci.bento.search.query.yaml.filter.YamlFilterType;
import gov.nih.nci.bento.search.query.yaml.filter.YamlGlobalFilterType;
import gov.nih.nci.bento.search.query.yaml.filter.YamlHighlight;
import gov.nih.nci.bento.search.result.TypeMapper;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.EsSearch;
import gov.nih.nci.bento.utility.StrUtil;
import graphql.schema.DataFetcher;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
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
        Yaml yaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery singleTypeQuery = yaml.load(new ClassPathResource(YAML_QUERY.FILE_NAMES.SINGLE).getInputStream());
        Map<String, DataFetcher> singleQueryMap = new HashMap<>();
        singleTypeQuery.getQuery().forEach(q->{
            singleQueryMap.put(q.getName(), env -> getYamlQuery(esService.CreateQueryParam(env), q));
        });
        assertThat(singleQueryMap.size(), equalTo(singleTypeQuery.getQuery().size()));
    }

    @Test
    public void groupQueryYaml_Test() throws IOException {
        Yaml yaml = new Yaml(new Constructor(GroupTypeQuery.class));
        GroupTypeQuery groupTypeQuery = yaml.load(new ClassPathResource(YAML_QUERY.FILE_NAMES.GROUP).getInputStream());
        Map<String, DataFetcher> groupQueryMap = new HashMap<>();

        groupTypeQuery.getGroups().forEach(group->{
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
        Yaml yaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery singleTypeQuery = yaml.load(new ClassPathResource(YAML_QUERY.FILE_NAMES.GLOBAL).getInputStream());
        Map<String, DataFetcher> singleQueryMap = new HashMap<>();
        singleTypeQuery.getQuery().forEach(q->{
            singleQueryMap.put(q.getName(), env -> getYamlQuery(esService.CreateQueryParam(env), q));
        });
        assertThat(singleQueryMap.size(), equalTo(singleTypeQuery.getQuery().size()));
    }


    private Object createGroupQuery(GroupTypeQuery.Group group, QueryParam param) throws IOException {
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
        switch (filterType.getType()) {
            case YAML_QUERY.FILTER.AGGREGATION:
                return new AggregationFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .isExcludeFilter(filterType.isFilter())
                                .selectedField(filterType.getSelectedField())
                                .build())
                        .getSourceFilter();
            case YAML_QUERY.FILTER.TABLE:
                return new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        // TODO
//                    .customOrderBy(getIntCustomOrderBy(param))
                        .defaultSortField(filterType.getDefaultSortField())
                        .build()).getSourceFilter();
            case YAML_QUERY.FILTER.NO_OF_DOCUMENTS:
                return new SearchCountFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .build())
                        .getSourceFilter();
            case YAML_QUERY.FILTER.RANGE:
                return new RangeFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .selectedField(filterType.getSelectedField())
                                .isExcludeFilter(true)
                                .build())
                        .getSourceFilter();
            case YAML_QUERY.FILTER.SUB_AGGREAGATION:
                return new SubAggregationFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .selectedField(filterType.getSelectedField())
                                .subAggSelectedField(filterType.getSubAggSelectedField())
                                .build())
                        .getSourceFilter();
            case YAML_QUERY.FILTER.DEFAULT:
                return new DefaultFilter(FilterParam.builder()
                        .args(param.getArgs()).build()).getSourceFilter();
            case YAML_QUERY.FILTER.GLOBAL:
                return createGlobalQuery(param,query);
            default:
                throw new IllegalArgumentException();
        }
    }


    private SearchSourceBuilder createGlobalQuery(QueryParam param, YamlQuery query) {
        TableParam tableParam = param.getTableParam();
        // Store Conditional Query
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(tableParam.getPageSize())
                .from(tableParam.getOffSet())
                .query(
                        addConditionalQuery(
                                createGlobalQuerySets(param, query),
                                createGlobalConditionalQueries(param, query))
                );
        // Set Sort
        if (query.getFilterType().getDefaultSortField() !=null) builder.sort(query.getFilterType().getDefaultSortField(), SortOrder.DESC);
        // Set Highlight Query
        setGlobalHighlightQuery(query, builder);
        return builder;
    }

    private void setGlobalHighlightQuery(YamlQuery query, SearchSourceBuilder builder) {
        if (query.getHighlight() != null) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            YamlHighlight yamlHighlight = query.getHighlight();
            // Set Multiple Highlight Fields
            yamlHighlight.getFields().forEach((f)->highlightBuilder.field(f));
            highlightBuilder.preTags(yamlHighlight.getPreTag() == null ? "" : yamlHighlight.getPreTag());
            highlightBuilder.postTags(yamlHighlight.getPostTag() == null ? "" : yamlHighlight.getPostTag());
            if (highlightBuilder.fragmentSize() != null) highlightBuilder.fragmentSize(yamlHighlight.getFragmentSize());
            builder.highlighter(highlightBuilder);
        }
    }

    private BoolQueryBuilder createGlobalQuerySets(QueryParam param, YamlQuery query) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        List<YamlGlobalFilterType.GlobalQuerySet> globalQuerySets = query.getFilterType().getQuery();
        // Add Should Query
        globalQuerySets.forEach(globalQuery -> {
            if (globalQuery.getType().equals(YAML_QUERY.QUERY_TERMS.TERM)) {
                boolQueryBuilder.should(QueryBuilders.termQuery(globalQuery.getField(), param.getSearchText()));
            } else if (globalQuery.getType().equals(YAML_QUERY.QUERY_TERMS.WILD_CARD)) {
                boolQueryBuilder.should(QueryBuilders.wildcardQuery(globalQuery.getField(), "*" + param.getSearchText()+ "*").caseInsensitive(true));
            } else if (globalQuery.getType().equals(YAML_QUERY.QUERY_TERMS.MATCH)) {
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
            if (option.getOption().equals(YAML_QUERY.QUERY_TERMS.BOOLEAN)) {
                filterString = StrUtil.getBoolText(param.getSearchText());
            } else if (option.getOption().equals(YAML_QUERY.QUERY_TERMS.INTEGER)) {
                filterString = StrUtil.getIntText(param.getSearchText());
            } else {
                throw new IllegalArgumentException();
            }

            if (option.getType().equals(YAML_QUERY.QUERY_TERMS.MATCH)) {
                conditionalList.add(QueryBuilders.matchQuery(option.getField(), filterString));
            } else if (option.getType().equals(YAML_QUERY.QUERY_TERMS.TERM)) {
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
            if (q.getName().equals(YAML_QUERY.QUERY_TERMS.MATCH)) {
                MatchQueryBuilder matchQuery = getQuery(q);
                if (!matchQuery.value().equals("")) builder.should(q);
            } else if (q.getName().equals(YAML_QUERY.QUERY_TERMS.TERM)) {
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
            case YAML_QUERY.RESULT_TYPE.DEFAULT:
                return typeMapper.getDefault(param.getReturnTypes());
            case YAML_QUERY.RESULT_TYPE.AGGREGATION:
                return typeMapper.getAggregate();
            case YAML_QUERY.RESULT_TYPE.INT_TOTAL_AGGREGATION:
                return typeMapper.getAggregateTotalCnt();
            case YAML_QUERY.RESULT_TYPE.RANGE:
                return typeMapper.getRange();
            case YAML_QUERY.RESULT_TYPE.ARM_PROGRAM:
                return typeMapper.getArmProgram();
            case YAML_QUERY.RESULT_TYPE.INT_TOTAL_COUNT:
                return typeMapper.getIntTotal();
            case YAML_QUERY.RESULT_TYPE.STRING_LIST:
                return typeMapper.getStrList(query.getFilterType().getSelectedField());
            case YAML_QUERY.RESULT_TYPE.GLOBAL_ABOUT:
                return typeMapper.getHighLightFragments(Const.BENTO_FIELDS.CONTENT_PARAGRAPH,
                        (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.ABOUT,
                                Const.BENTO_FIELDS.PAGE, source.get(Const.BENTO_FIELDS.PAGE),
                                Const.BENTO_FIELDS.TITLE,source.get(Const.BENTO_FIELDS.TITLE),
                                Const.BENTO_FIELDS.TEXT, text));
            case YAML_QUERY.RESULT_TYPE.GLOBAL:
                return typeMapper.getDefaultReturnTypes(param.getGlobalSearchResultTypes());
            case YAML_QUERY.RESULT_TYPE.GLOBAL_MULTIPLE_MODEL:
                return typeMapper.getMapWithHighlightedFields(param.getGlobalSearchResultTypes());
            default:
                throw new IllegalArgumentException();
        }
    }

}
