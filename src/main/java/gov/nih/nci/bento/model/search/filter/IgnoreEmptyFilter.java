package gov.nih.nci.bento.model.search.filter;

import gov.nih.nci.bento.constants.Const;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.*;

public class IgnoreEmptyFilter {

    private final FilterParam param;
    private boolean isIgnoreCondition = false;
    private SearchSourceBuilder ignoreSearches;

    public IgnoreEmptyFilter(FilterParam param) {
        this.param = param;
        if (isAllEmptyArray()) {
            isIgnoreCondition = true;
            ignoreSearches = emptyArrayQuery();
        }

        if (isAllContainEmptyString()) {
            isIgnoreCondition = true;
            ignoreSearches = getAllDataQuery();
        }
    }

    private SearchSourceBuilder getAllDataQuery() {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(Const.ES_UNITS.MAX_SIZE);
        return builder;
    }

    private boolean isAllEmptyArray() {
        Map<String, Object> args = new HashMap<>(param.getArgs());
        List<List<String>> values = fillRequiredElements(args, param);
        boolean emptyArrayCondition = values.size() > 0 && values.stream().allMatch(item->item.size()==0);
        return isDesiredFieldsExisted(args, param) && emptyArrayCondition;
    }

    private boolean isAllContainEmptyString() {
        Map<String, Object> args = new HashMap<>(param.getArgs());
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

    private SearchSourceBuilder emptyArrayQuery() {
        return new SearchSourceBuilder().size(0);
    }

    public boolean isIgnoreCondition() {
        return isIgnoreCondition;
    }

    public SearchSourceBuilder getIgnoreSearches() {
        return ignoreSearches;
    }
}
