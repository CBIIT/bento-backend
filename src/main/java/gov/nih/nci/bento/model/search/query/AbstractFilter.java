package gov.nih.nci.bento.model.search.query;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static gov.nih.nci.bento.constants.Const.getOppositeTempQueryParamMap;

public abstract class AbstractFilter {
    // Not Filter Parameters
    private Set<String> sortParams = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
    private Map<String, Object> args;

    private FilterParam param;

    public AbstractFilter(FilterParam param) {
        this.param = param;
        Map<String, Object> map = new HashMap<>(param.getArgs());
        removeSortParams(map);
        // Filter; excludes its field
        // TODO
        Map<String, String> keyMap= getOppositeTempQueryParamMap();
        if (param.isExcludeFilter()) {
            String excludedKey = keyMap.getOrDefault(param.getSelectedField(), param.getSelectedField());
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
        return getFilter(param, args);
    }

    abstract SearchSourceBuilder getFilter(FilterParam param, Map<String, Object> args);
}
