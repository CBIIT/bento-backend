package gov.nih.nci.bento.model.search.filter;

import gov.nih.nci.bento.model.search.query.QueryFactory;
import org.opensearch.search.builder.SearchSourceBuilder;

public abstract class AbstractFilter {
    private final QueryFactory bentoParam;
    private final FilterParam param;
    private final IgnoreEmptyFilter ignoreEmptyFilter;

    abstract SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam);

    public AbstractFilter(FilterParam param) {
        this.param = param;
        ignoreEmptyFilter = new IgnoreEmptyFilter(param);
        bentoParam = new QueryFactory(param);
    }

    public SearchSourceBuilder getSourceFilter() {
        if (ignoreEmptyFilter.isIgnoreCondition()) return ignoreEmptyFilter.getIgnoreSearches();
        return getFilter(param, bentoParam);
    }
}
