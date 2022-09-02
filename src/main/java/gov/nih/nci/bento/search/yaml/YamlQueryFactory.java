package gov.nih.nci.bento.search.yaml;

import gov.nih.nci.bento.classes.*;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.result.TypeMapper;
import gov.nih.nci.bento.search.result.TypeMapperService;
import gov.nih.nci.bento.search.yaml.filter.*;
import gov.nih.nci.bento.service.EsSearch;
import gov.nih.nci.bento.utility.StrUtil;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.*;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class YamlQueryFactory {

    private final EsSearch esService;
    private final TypeMapperService typeMapper;
    private static final Logger logger = LogManager.getLogger(YamlQueryFactory.class);

    public Map<String, DataFetcher> createYamlQueries() throws IOException {
        logger.info("Yaml File Queries Loaded");
        // Set Single Request API
        Yaml yaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery singleTypeQuery = yaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_BENTO.SINGLE).getInputStream());
        Map<String, graphql.schema.DataFetcher> result = new HashMap<>();
        singleTypeQuery.getQueries().forEach(q->
            result.put(q.getName(), env -> createSingleYamlQuery(esService.CreateQueryParam(env), q))
        );
        // Set Group Request API
        Yaml groupYaml = new Yaml(new Constructor(GroupTypeQuery.class));
        GroupTypeQuery groupTypeQuery = groupYaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_BENTO.GROUP).getInputStream());
        groupTypeQuery.getQueries().forEach(group->{
            String queryName = group.getName();
            result.put(queryName, env -> createGroupYamlQuery(group, esService.CreateQueryParam(env)));
        });
        // Set Global Search Request API
        Yaml globalYaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery globalQuery = globalYaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_BENTO.GLOBAL).getInputStream());
        globalQuery.getQueries().forEach(q->
            result.put(q.getName(), env -> createGlobalYamlQuery(esService.CreateQueryParam(env), q))
        );
        return result;
    }


    public Map<String, DataFetcher> createICDCYamlQueries() throws IOException {
        logger.info("Yaml File Queries Loaded");
        // Set Single Request API
        Yaml yaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery singleTypeQuery = yaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_ICDC.SINGLE).getInputStream());
        Map<String, graphql.schema.DataFetcher> result = new HashMap<>();
        singleTypeQuery.getQueries().forEach(q->
                result.put(q.getName(), env -> createSingleYamlQuery(esService.CreateQueryParam(env), q))
        );
        // Set Group Request API
        Yaml groupYaml = new Yaml(new Constructor(GroupTypeQuery.class));
        GroupTypeQuery groupTypeQuery = groupYaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_ICDC.GROUP).getInputStream());
        groupTypeQuery.getQueries().forEach(group->{
            String queryName = group.getName();
            result.put(queryName, env -> createGroupYamlQuery(group, esService.CreateQueryParam(env)));
        });
//        // Set Global Search Request API
//        Yaml globalYaml = new Yaml(new Constructor(SingleTypeQuery.class));
//        SingleTypeQuery globalQuery = globalYaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_BENTO.GLOBAL).getInputStream());
//        globalQuery.getQuery().forEach(q->
//            result.put(q.getName(), env -> createGlobalYamlQuery(esService.CreateQueryParam(env), q))
//        );
        return result;
    }

    private Object createGroupYamlQuery(GroupTypeQuery.Group group, QueryParam param) {
        logger.info("Group Search API Requested: " + group.getName());
        List<MultipleRequests> requests = new ArrayList<>();
        group.getReturnFields().forEach(q->{
            MultipleRequests multipleRequest = MultipleRequests.builder()
                    .name(q.getName())
                    .request(new SearchRequest()
                            .indices(q.getIndex())
                            .source(getQueryFilter(param, q)))
                    .typeMapper(getReturnType(param, q)).build();
            requests.add(multipleRequest);

        });
        return esService.elasticMultiSend(requests);
    }

    private TypeMapper getReturnType(QueryParam param, YamlQuery query) {
        // Set Result Type
        String method = query.getResult().getMethod();
        switch (query.getResult().getType()) {
            case Const.YAML_QUERY.RESULT_TYPE.OBJECT_ARRAY:
                return typeMapper.getList(param.getReturnTypes());
            case Const.YAML_QUERY.RESULT_TYPE.ICDC_AGGREGATION:
                return typeMapper.getICDCAggregate();
            case Const.YAML_QUERY.RESULT_TYPE.GROUP_COUNT:
                return typeMapper.getAggregate();
            case Const.YAML_QUERY.RESULT_TYPE.INT:
                if (method.equals("count_bucket_keys")) {
                    return typeMapper.getAggregateTotalCnt();
                } else if (method.equals("nested_count")) {
                    return typeMapper.getNestedAggregateTotalCnt();
                }
                throw new IllegalArgumentException("Automatic GraphQL mapper: Illegal int return types");
            case Const.YAML_QUERY.RESULT_TYPE.FLOAT:
                if (query.getResult().getMethod().equals(Const.YAML_QUERY.RESULT_TYPE.SUM_AGG)) return typeMapper.getSumAggregate();
                throw new IllegalArgumentException("Automatic GraphQL mapper: Illegal float return types");
            case Const.YAML_QUERY.RESULT_TYPE.RANGE:
                return typeMapper.getRange();
            case Const.YAML_QUERY.RESULT_TYPE.ARM_PROGRAM:
                return typeMapper.getArmProgram();
//                TODO DUPLICATE ICDC ARM
            case Const.YAML_QUERY.RESULT_TYPE.ICDC_ARM_PROGRAM:
                return typeMapper.getICDCArmProgram();
            case Const.YAML_QUERY.RESULT_TYPE.INT_TOTAL_COUNT:
                return typeMapper.getIntTotal();
            case Const.YAML_QUERY.RESULT_TYPE.STRING_ARRAY:
                return typeMapper.getStrList(query.getFilter().getSelectedField());
            case Const.YAML_QUERY.RESULT_TYPE.GLOBAL_ABOUT:
                return typeMapper.getHighLightFragments(query.getFilter().getSelectedField(),
                        (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.ABOUT,
                                Const.BENTO_FIELDS.PAGE, source.get(Const.BENTO_FIELDS.PAGE),
                                Const.BENTO_FIELDS.TITLE,source.get(Const.BENTO_FIELDS.TITLE),
                                Const.BENTO_FIELDS.TEXT, text));
            case Const.YAML_QUERY.RESULT_TYPE.GLOBAL:
                return typeMapper.getQueryResult(param.getGlobalSearchResultTypes());
            case Const.YAML_QUERY.RESULT_TYPE.NESTED:
                return typeMapper.getNestedAggregate();
            case Const.YAML_QUERY.RESULT_TYPE.NESTED_LIST:
                return typeMapper.getNestedAggregateList();
            // TODO TO BE DELETED
            case Const.YAML_QUERY.RESULT_TYPE.ICDC_NESTED_LIST:
                return typeMapper.getICDCNestedAggregateList();
            case Const.YAML_QUERY.RESULT_TYPE.NESTED_TOTAL:
                return typeMapper.getIntTotalNestedAggregate();
            case Const.YAML_QUERY.RESULT_TYPE.GLOBAL_MULTIPLE_MODEL:
                return typeMapper.getMapWithHighlightedFields(param.getGlobalSearchResultTypes());
            case Const.YAML_QUERY.RESULT_TYPE.EMPTY:
                return response -> 0;
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

    private Object createGlobalYamlQuery(QueryParam param, YamlQuery query) {
        logger.info("ES Search Global API Requested: " + query.getName());
        Map<String, Object> result = new HashMap<>();
        // Set Bool Filter
        SearchSourceBuilder builder = getQueryFilter(param, query);
        MultipleRequests request = MultipleRequests.builder()
                .name(query.getName())
                .request(new SearchRequest()
                        .indices(query.getIndex())
                        .source(builder))
                .typeMapper(getReturnType(param, query)).build();
        Map<String, Object> multiResult = esService.elasticMultiSend(List.of(request));
        QueryResult queryResult = (QueryResult) multiResult.get(query.getName());
        List<Map<String, Object>> searchHits_Test = queryResult.getSearchHits();
        result.put("result", checkEmptySearch(param, searchHits_Test));
        result.put("count", checkEmptySearch(param, queryResult.getTotalHits()));
        return result;
    }

    private Object createSingleYamlQuery(QueryParam param, YamlQuery query) {
        logger.info("ES Search API Requested: " + query.getName());
        // Set Rest API Request
        SearchRequest request = new SearchRequest();
        request.indices(query.getIndex());
        request.source(getQueryFilter(param, query));
        return esService.elasticSend(request, getReturnType(param, query));
    }

    private Map<String, Object> getDynamicFilter(QueryParam param, YamlQuery query) {
        if (query.getDynamicFilter() == null) return param.getArgs();
        YamlDynamicFilter dynamicFilter = query.getDynamicFilter();
        List<MultipleRequests> requests = List.of(
                MultipleRequests.builder()
                        .name(Const.ES_PARAMS.TERMS_AGGS)
                        .request(new SearchRequest()
                                .indices(dynamicFilter.getIndex())
                                .source(new AggregationFilter(
                                        FilterParam.builder()
                                                .args(param.getArgs())
                                                .selectedField(dynamicFilter.getDynamicField())
                                                .build())
                                        .getSourceFilter()))
                        .typeMapper(typeMapper.getICDCCaseIds()).build()
        );

        Map<String, Object> result = esService.elasticMultiSend(requests);
        List<String> caseIds = (List<String>) result.get(Const.ES_PARAMS.TERMS_AGGS);
        if (caseIds.size() > 0) {
            Map<String, Object> args = new HashMap<>(param.getArgs());
            args.put(dynamicFilter.getTargetField(), caseIds);
            return args;
        }
        return param.getArgs();
    }

    private SearchSourceBuilder getQueryFilter(QueryParam param, YamlQuery query) {
        // Set Arguments
        YamlFilter filterType = query.getFilter();
        Map<String, Object> args = getDynamicFilter(param, query);
        switch (filterType.getType()) {
            case Const.YAML_QUERY.FILTER.AGGREGATION:
                return new AggregationFilter(
                        FilterParam.builder()
                                .args(args)
                                .isExcludeFilter(filterType.isIgnoreSelectedField())
                                .selectedField(filterType.getSelectedField())
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.PAGINATION:
                return new PaginationFilter(FilterParam.builder()
                        .args(args)
                        .queryParam(param)
                        .sortDirection(filterType.getSortDirection())
                        .ignoreIfEmpty(filterType.getIgnoreIfEmpty())
                        .customOrderBy(getIntCustomOrderBy_Test(param, query))
                        .defaultSortField(filterType.getDefaultSortField())
                        .build()).getSourceFilter();
            case Const.YAML_QUERY.FILTER.RANGE:
                return new RangeFilter(
                        FilterParam.builder()
                                .args(args)
                                .selectedField(filterType.getSelectedField())
                                .isRangeFilter(true)
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.SUB_AGGREAGATION:
                return new SubAggregationFilter(
                        FilterParam.builder()
                                .args(args)
                                .selectedField(filterType.getSelectedField())
                                .subAggSelectedField(filterType.getSubAggSelectedField())
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.DEFAULT:
                return new DefaultFilter(FilterParam.builder()
                        .size(filterType.getSize())
                        .ignoreIfEmpty(filterType.getIgnoreIfEmpty())
                        .caseInsensitive(filterType.isCaseInsensitive())
                        .args(args).build()).getSourceFilter();
            case Const.YAML_QUERY.FILTER.NESTED:
                return new NestedFilter(
                        FilterParam.builder()
                                .args(args)
                                .isExcludeFilter(filterType.isIgnoreSelectedField())
                                .selectedField(filterType.getSelectedField())
                                .nestedPath(filterType.getNestedPath())
                                .nestedParameters(filterType.getNestedParameters())
                                .build())
                        .getSourceFilter();
            case Const.YAML_QUERY.FILTER.GLOBAL:
                return createGlobalQuery(param,query);
            case Const.YAML_QUERY.FILTER.SUM:
                return new SumFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(filterType.getSelectedField())
                                        .build())
                                .getSourceFilter();
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
        if (query.getFilter().getDefaultSortField() !=null) builder.sort(query.getFilter().getDefaultSortField(), SortOrder.DESC);
        // Set Highlight Query
        setGlobalHighlightQuery(query, builder);
        return builder;
    }

    private List<QueryBuilder> createGlobalConditionalQueries(QueryParam param, YamlQuery query) {
        if (query.getFilter().getTypedSearch() == null) return new ArrayList<>();
        List<QueryBuilder> conditionalList = new ArrayList<>();
        List<YamlGlobalFilterType.GlobalQuerySet> typeQuerySets = query.getFilter().getTypedSearch() ;
        AtomicReference<String> filterString = new AtomicReference<>("");
        typeQuerySets.forEach(option-> {
            if (option.getOption().equals(Const.YAML_QUERY.QUERY_TERMS.BOOLEAN)) {
                filterString.set(StrUtil.getBoolText(param.getSearchText()));
            } else if (option.getOption().equals(Const.YAML_QUERY.QUERY_TERMS.INTEGER)) {
                filterString.set(StrUtil.getIntText(param.getSearchText()));
            } else {
                throw new IllegalArgumentException();
            }

            if (option.getType().equals(Const.YAML_QUERY.QUERY_TERMS.MATCH)) {
                conditionalList.add(QueryBuilders.matchQuery(option.getField(), filterString));
            } else if (option.getType().equals(Const.YAML_QUERY.QUERY_TERMS.TERM)) {
                conditionalList.add(QueryBuilders.termQuery(option.getField(), filterString.get()));
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
            yamlHighlight.getFields().forEach(highlightBuilder::field);
            highlightBuilder.preTags(yamlHighlight.getPreTag() == null ? "" : yamlHighlight.getPreTag());
            highlightBuilder.postTags(yamlHighlight.getPostTag() == null ? "" : yamlHighlight.getPostTag());
            if (highlightBuilder.fragmentSize() != null) highlightBuilder.fragmentSize(yamlHighlight.getFragmentSize());
            builder.highlighter(highlightBuilder);
        }
    }

    private BoolQueryBuilder createGlobalQuerySets(QueryParam param, YamlQuery query) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        List<YamlGlobalFilterType.GlobalQuerySet> globalQuerySets = query.getFilter().getSearches();
        // Add Should Query
        globalQuerySets.forEach(globalQuery -> {
            switch (globalQuery.getType()) {
                case Const.YAML_QUERY.QUERY_TERMS.TERM:
                    boolQueryBuilder.should(QueryBuilders.termQuery(globalQuery.getField(), param.getSearchText()));
                    break;
                case Const.YAML_QUERY.QUERY_TERMS.WILD_CARD:
                    boolQueryBuilder.should(QueryBuilders.wildcardQuery(globalQuery.getField(), "*" + param.getSearchText()+ "*").caseInsensitive(true));
                    break;
                case Const.YAML_QUERY.QUERY_TERMS.MATCH:
                    boolQueryBuilder.should(QueryBuilders.matchQuery(globalQuery.getField(), param.getSearchText()));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        });
        return boolQueryBuilder;
    }

    private String getIntCustomOrderBy_Test(QueryParam param, YamlQuery query) {
        String orderKey = param.getTableParam().getOrderBy();
        if (query.getFilter().getAlternativeSortField() == null) return orderKey;
        Map<String, String> alternativeSortMap = query.getFilter().getAlternativeSortField();
        return alternativeSortMap.getOrDefault(orderKey, "");
    }
}
