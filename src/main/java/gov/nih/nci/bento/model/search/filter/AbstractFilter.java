package gov.nih.nci.bento.model.search.filter;

import gov.nih.nci.bento.model.search.query.QueryFactory;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractFilter {
    private final QueryFactory bentoParam;
    private final FilterParam param;

    abstract SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam);

    public AbstractFilter(FilterParam param) {
        this.param = param;
        Map<String, Object> map = new HashMap<>(param.getArgs());
        bentoParam = new QueryFactory(map);
    }

    public SearchSourceBuilder getSourceFilter() {
        return getFilter(param, bentoParam);
    }
}
