package gov.nih.nci.bento.search.yaml;

import gov.nih.nci.bento.classes.*;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.result.TypeMapper;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.search.yaml.filter.YamlFilterType;
import gov.nih.nci.bento.search.yaml.filter.YamlGlobalFilterType;
import gov.nih.nci.bento.search.yaml.filter.YamlHighlight;
import gov.nih.nci.bento.search.yaml.filter.YamlQuery;
import gov.nih.nci.bento.service.EsSearch;
import gov.nih.nci.bento.utility.StrUtil;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class YamlQueryBuilder {

    private final EsSearch esService;
    private final TypeMapperImpl typeMapper;
    private static final Logger logger = LogManager.getLogger(YamlQueryBuilder.class);

    public Map<String, DataFetcher> createYamlQueries() throws IOException {
        logger.info("Yaml File Queries Loaded");
        Yaml yaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery singleTypeQuery = yaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES.SINGLE).getInputStream());
        Map<String, graphql.schema.DataFetcher> map = new HashMap<>();
        singleTypeQuery.getQuery().forEach(q->{
            map.put(q.getName(), env -> getYamlQuery(esService.CreateQueryParam(env), q));
        });

        Yaml groupYaml = new Yaml(new Constructor(GroupTypeQuery.class));
        GroupTypeQuery groupTypeQuery = groupYaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES.GROUP).getInputStream());
        groupTypeQuery.getGroups().forEach(group->{
            String queryName = group.getName();
            map.put(queryName, env -> createGroupQuery(group, esService.CreateQueryParam(env)));
        });

        Yaml globalYaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery globalQuery = globalYaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES.GLOBAL).getInputStream());
        globalQuery.getQuery().forEach(q->{
            map.put(q.getName(), env -> getYamlGlobalQuery(esService.CreateQueryParam(env), q));
        });
        return map;
    }

    private Object createGroupQuery(GroupTypeQuery.Group group, QueryParam param) throws IOException {
        logger.info("Group Search API Requested: " + group.getName());
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
        return esService.elasticMultiSend(requests);
    }

    private TypeMapper getTypeMapper(QueryParam param, YamlQuery query) {
        // Set Result Type

        switch (query.getResultType()) {
            case Const.YAML_QUERY.RESULT_TYPE.DEFAULT:
                return typeMapper.getDefault(param.getReturnTypes());
            case Const.YAML_QUERY.RESULT_TYPE.AGGREGATION:
                return typeMapper.getAggregate();
            case Const.YAML_QUERY.RESULT_TYPE.INT_TOTAL_AGGREGATION:
                return typeMapper.getAggregateTotalCnt();
            case Const.YAML_QUERY.RESULT_TYPE.RANGE:
                return typeMapper.getRange();
            case Const.YAML_QUERY.RESULT_TYPE.ARM_PROGRAM:
                return typeMapper.getArmProgram();
            case Const.YAML_QUERY.RESULT_TYPE.INT_TOTAL_COUNT:
                return typeMapper.getIntTotal();
            case Const.YAML_QUERY.RESULT_TYPE.STRING_LIST:
                return typeMapper.getStrList(query.getFilterType().getSelectedField());
            case Const.YAML_QUERY.RESULT_TYPE.GLOBAL_ABOUT:
                return typeMapper.getHighLightFragments(query.getFilterType().getSelectedField(),
                        (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.ABOUT,
                                Const.BENTO_FIELDS.PAGE, source.get(Const.BENTO_FIELDS.PAGE),
                                Const.BENTO_FIELDS.TITLE,source.get(Const.BENTO_FIELDS.TITLE),
                                Const.BENTO_FIELDS.TEXT, text));
            case Const.YAML_QUERY.RESULT_TYPE.GLOBAL:
                return typeMapper.getDefaultReturnTypes(param.getGlobalSearchResultTypes());
            case Const.YAML_QUERY.RESULT_TYPE.GLOBAL_MULTIPLE_MODEL:
                return typeMapper.getMapWithHighlightedFields(param.getGlobalSearchResultTypes());
            default:
                throw new IllegalArgumentException();
        }
    }
    // TODO TO BE DELETED
    private List<?> checkEmptySearch(QueryParam param, List<Map<String, Object>> result) {
        return param.getSearchText().equals("") ? new ArrayList<>() : result;
    }
    // TODO TO BE DELETED
    private int checkEmptySearch(QueryParam param, int result) {
        return param.getSearchText().equals("") ? 0 :result;
    }

    private Object getYamlGlobalQuery(QueryParam param, YamlQuery query) throws IOException {
        logger.info("ES Search Global API Requested: " + query.getName());
        Map<String, Object> result = new HashMap<>();
        // Set Bool Filter
        SearchSourceBuilder builder = getSourceBuilder(param, query);
        MultipleRequests request = MultipleRequests.builder()
                .name(query.getName())
                .request(new SearchRequest()
                        .indices(query.getIndex())
                        .source(builder))
                .typeMapper(getTypeMapper(param, query)).build();
        Map<String, Object> multiResult = esService.elasticMultiSend(List.of(request));
        QueryResult queryResult = (QueryResult) multiResult.get(query.getName());
        List<Map<String, Object>> searchHits_Test = queryResult.getSearchHits();
        result.put("result", checkEmptySearch(param, searchHits_Test));
        result.put("count", checkEmptySearch(param, queryResult.getTotalHits()));
        return result;
    }

    private Object getYamlQuery(QueryParam param, YamlQuery query) throws IOException {
        logger.info("ES Search API Requested: " + query.getName());
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
            case Const.YAML_QUERY.FILTER.AGGREGATION:
                return new AggregationFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .isExcludeFilter(filterType.isFilter())
                                .selectedField(filterType.getSelectedField())
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.TABLE:
                return new TableFilter(FilterParam.builder()
                        .args(param.getArgs())
                        .queryParam(param)
                        .customOrderBy(getIntCustomOrderBy_Test(param, query))
                        .defaultSortField(filterType.getDefaultSortField())
                        .build()).getSourceFilter();
            case Const.YAML_QUERY.FILTER.NO_OF_DOCUMENTS:
                return new SearchCountFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.RANGE:
                return new RangeFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .selectedField(filterType.getSelectedField())
                                .isExcludeFilter(true)
                                .isRangeFilter(true)
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.SUB_AGGREAGATION:
                return new SubAggregationFilter(
                        FilterParam.builder()
                                .args(param.getArgs())
                                .selectedField(filterType.getSelectedField())
                                .subAggSelectedField(filterType.getSubAggSelectedField())
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.DEFAULT:
                return new DefaultFilter(FilterParam.builder()
                        .args(param.getArgs()).build()).getSourceFilter();
            case Const.YAML_QUERY.FILTER.GLOBAL:
                return createGlobalQuery(param,query);
            default:
                throw new IllegalArgumentException();
        }
    }

    // Add Conditional Query
    private BoolQueryBuilder addConditionalQuery(BoolQueryBuilder builder, List<QueryBuilder> builders) {
        builders.forEach(q->{
            if (q.getName().equals(Const.YAML_QUERY.QUERY_TERMS.MATCH)) {
                MatchQueryBuilder matchQuery = getQuery(q);
                if (!matchQuery.value().equals("")) builder.should(q);
            } else if (q.getName().equals(Const.YAML_QUERY.QUERY_TERMS.TERM)) {
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

    private List<QueryBuilder> createGlobalConditionalQueries(QueryParam param, YamlQuery query) {
        if (query.getFilterType().getOptionalQuery() == null) return new ArrayList<>();
        List<QueryBuilder> conditionalList = new ArrayList<>();
        List<YamlGlobalFilterType.GlobalQuerySet> optionalQuerySets = query.getFilterType().getOptionalQuery() ;
        optionalQuerySets.forEach(option-> {
            String filterString = "";
            if (option.getOption().equals(Const.YAML_QUERY.QUERY_TERMS.BOOLEAN)) {
                filterString = StrUtil.getBoolText(param.getSearchText());
            } else if (option.getOption().equals(Const.YAML_QUERY.QUERY_TERMS.INTEGER)) {
                filterString = StrUtil.getIntText(param.getSearchText());
            } else {
                throw new IllegalArgumentException();
            }

            if (option.getType().equals(Const.YAML_QUERY.QUERY_TERMS.MATCH)) {
                conditionalList.add(QueryBuilders.matchQuery(option.getField(), filterString));
            } else if (option.getType().equals(Const.YAML_QUERY.QUERY_TERMS.TERM)) {
                conditionalList.add(QueryBuilders.termQuery(option.getField(), filterString));
            } else {
                throw new IllegalArgumentException();
            }
        });
        return conditionalList;
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
            if (globalQuery.getType().equals(Const.YAML_QUERY.QUERY_TERMS.TERM)) {
                boolQueryBuilder.should(QueryBuilders.termQuery(globalQuery.getField(), param.getSearchText()));
            } else if (globalQuery.getType().equals(Const.YAML_QUERY.QUERY_TERMS.WILD_CARD)) {
                boolQueryBuilder.should(QueryBuilders.wildcardQuery(globalQuery.getField(), "*" + param.getSearchText()+ "*").caseInsensitive(true));
            } else if (globalQuery.getType().equals(Const.YAML_QUERY.QUERY_TERMS.MATCH)) {
                boolQueryBuilder.should(QueryBuilders.matchQuery(globalQuery.getField(), param.getSearchText()));
            } else {
                throw new IllegalArgumentException();
            }
        });
        return boolQueryBuilder;
    }

    private String getIntCustomOrderBy_Test(QueryParam param, YamlQuery query) {
        String orderKey = param.getTableParam().getOrderBy();
        if (query.getFilterType().getAlternativeSort() == null) return orderKey;
        Map<String, String> alternativeSortMap = query.getFilterType().getAlternativeSort();
        return alternativeSortMap.getOrDefault(orderKey, "");
    }
}
