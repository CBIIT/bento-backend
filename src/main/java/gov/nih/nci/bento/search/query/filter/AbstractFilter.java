package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.BentoQueryCreator;
import gov.nih.nci.bento.search.query.QueryCreator;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        if (param.isExcludeFilter()) {
            // Consider Remove Keyword
            String key = param.getSelectedField();
            if (map.containsKey(key)) map.remove(key);
        }
        bentoParam = new BentoQueryCreator(map, param);
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
