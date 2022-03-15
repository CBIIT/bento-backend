package gov.nih.nci.bento.model.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.search.query.BentoQueryCreator;
import gov.nih.nci.bento.model.search.query.QueryCreator;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static gov.nih.nci.bento.constants.Const.getOppositeTempQueryParamMap;

public abstract class AbstractFilter {
    // Parameters Exceptions
    private final Set<String> sortParams = Set.of(Const.ES_PARAMS.ORDER_BY, Const.ES_PARAMS.SORT_DIRECTION, Const.ES_PARAMS.OFFSET, Const.ES_PARAMS.PAGE_SIZE);
    private final QueryCreator bentoParam;
    private final FilterParam param;

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
        bentoParam = new BentoQueryCreator(map);
    }

    private void removeSortParams(Map<String, Object> map) {
        sortParams.forEach(key -> {
            if (map.containsKey(key)) map.remove(key);
        });
    }

    public SearchSourceBuilder getSourceFilter() {
        return getFilter(param, bentoParam);
    }

    abstract SearchSourceBuilder getFilter(FilterParam param, QueryCreator bentoParam);
}
