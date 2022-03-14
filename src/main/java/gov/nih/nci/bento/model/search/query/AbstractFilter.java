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

    // TODO remove temp keys
    public AbstractFilter(Map<String, Object> params, String selectedField, boolean... isFilter) {
        Map<String, Object> map = new HashMap<>(params);
        this.selectedField = selectedField;
        sortParams.forEach(key -> {
            if (map.containsKey(key)) map.remove(key);
        });

        // Filter; excludes its field
        // TODO
        Map<String, String> keyMap= getOppositeTempQueryParamMap();
        if (isFilter.length > 0) {
            String excludedKey = keyMap.getOrDefault(selectedField, selectedField);
            if (map.containsKey(excludedKey)) map.remove(excludedKey);
        }
        args = map;
    }

    public SearchSourceBuilder getSourceFilter() {
        return getFilter(args, selectedField);
    }

    abstract SearchSourceBuilder getFilter(Map<String, Object> args, String selectedField);

}
