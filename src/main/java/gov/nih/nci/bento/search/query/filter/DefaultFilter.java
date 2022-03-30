package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class DefaultFilter extends AbstractFilter {

    public DefaultFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam) {
        return new SearchSourceBuilder()
                .query(
                        bentoParam.getQuery()
                );
    }
}
