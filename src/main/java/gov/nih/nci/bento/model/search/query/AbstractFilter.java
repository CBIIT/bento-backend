package gov.nih.nci.bento.model.search.query;

import gov.nih.nci.bento.constants.Const;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static gov.nih.nci.bento.constants.Const.getOppositeTempQueryParamMap;

public abstract class AbstractFilter {
    // Not Filter Parameters
    private Set<String> sortParams = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
    private Map<String, Object> args;
    private String selectedField;
    private String subAggField;

    // TODO remove temp keys
    public AbstractFilter(Map<String, Object> params, String selectedField, boolean... isFilter) {
        init(params, selectedField, isFilter);
    }

    public AbstractFilter(Map<String, Object> params, String selectedField, String subAggField) {
        this.selectedField = selectedField;
        this.subAggField = subAggField;
        Map<String, Object> map = new HashMap<>(params);
        removeSortParams(params);
        args = map;
    }

    private void init(Map<String, Object> params, String selectedField, boolean... isFilter) {
        this.selectedField = selectedField;
        Map<String, Object> map = new HashMap<>(params);
        removeSortParams(map);
        // Filter; excludes its field
        // TODO
        Map<String, String> keyMap= getOppositeTempQueryParamMap();
        if (isFilter.length > 0) {
            String excludedKey = keyMap.getOrDefault(selectedField, selectedField);
            if (map.containsKey(excludedKey)) map.remove(excludedKey);
        }
        args = map;
    }

    private void removeSortParams(Map<String, Object> map) {
        sortParams.forEach(key -> {
            if (map.containsKey(key)) map.remove(key);
        });
    }

    public SearchSourceBuilder getSourceFilter() {
        return getFilter(args, selectedField);
    }
    public SearchSourceBuilder getSubAggSourceFilter() {
        return getSubAggFilter(args, selectedField, subAggField);
    }

    abstract SearchSourceBuilder getFilter(Map<String, Object> args, String selectedField);
    abstract SearchSourceBuilder getSubAggFilter(Map<String, Object> args, String selectedField, String subAggField);

}
