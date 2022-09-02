package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.TableParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.BentoQueryFactory;
import gov.nih.nci.bento.search.query.QueryFactory;
import gov.nih.nci.bento.utility.ElasticUtil;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.*;

import static gov.nih.nci.bento.utility.ElasticUtil.getSortDirection;

public abstract class AbstractFilter {
    // Parameters Exceptions
    private final Set<String> sortParams = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
    private final QueryFactory bentoParam;
    private final FilterParam param;

    public AbstractFilter(FilterParam param) {
        this.param = param;
        Map<String, Object> map = new HashMap<>(param.getArgs());
        removeSortParams(map);
        // Filter; excludes its field
        if (param.isExcludeFilter() || param.isRangeFilter()) {
            // Consider Remove Keyword
            String key = param.getSelectedField();
            if (map.containsKey(key)) map.remove(key);
        }
        bentoParam = new BentoQueryFactory(map, param);
    }

    private void removeSortParams(Map<String, Object> map) {
        sortParams.forEach(key -> {
            if (map.containsKey(key)) map.remove(key);
        });
    }

    public SearchSourceBuilder getSourceFilter() {
        // Exception Case; If all list contains empty string, give all documents
        Map<String, Object> args = new HashMap<>(param.getArgs());
        removeSortParams(args);
        if (isAllContainEmptyString(args, param)) return getAllDataQuery();
        // Exception Case; If all arrays are empty, return nothing
        if (isAllEmptyArray(args, param)) return new SearchSourceBuilder().size(0);
        return getFilter(param, bentoParam);
    }

    private SearchSourceBuilder getAllDataQuery() {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(Const.ES_UNITS.MAX_SIZE);
        TableParam tableParam = param.getTableParam();
        if (tableParam !=null) {
            builder.size(tableParam.getPageSize());
            builder.sort(ElasticUtil.getAlternativeSortType(param),
                    getSortDirection(param));
            builder.from(tableParam.getOffSet());
        }
        return builder;
    }

    private boolean isAllEmptyArray(Map<String, Object> args, FilterParam param) {
        List<List<String>> values = fillRequiredElements(args, param);
        boolean emptyArrayCondition = values.size() > 0 && values.stream().allMatch(item->item.size()==0);
        return isDesiredFieldsExisted(args, param) && emptyArrayCondition;
    }

    private boolean isAllContainEmptyString(Map<String, Object> args, FilterParam param) {
        List<List<String>> values = fillRequiredElements(args, param);
        boolean emptyStrCondition = values.size() > 0 && values.stream().allMatch(item->item.size()==1 && item.contains(""));
        return isDesiredFieldsExisted(args, param) && emptyStrCondition;
    }

    private boolean isDesiredFieldsExisted(Map<String, Object> args, FilterParam param) {
        Set<String> conditionFieldSet = param.getIgnoreIfEmpty();
        return conditionFieldSet != null && conditionFieldSet.size() > 0 && conditionFieldSet.stream().allMatch(item->args.containsKey(item));
    }

    private List<List<String>> fillRequiredElements(Map<String, Object> args, FilterParam param) {
        Set<String> conditionFieldSet = param.getIgnoreIfEmpty();
        List<List<String>> values = new ArrayList<>();
        if (conditionFieldSet == null) return values;
        args.forEach((k, v) -> {
            if (conditionFieldSet.contains(k)) values.add((List<String>) args.get(k));
        });
        return values;
    }

    abstract SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam);
}
