package gov.nih.nci.bento.model.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.model.search.query.QueryCreator;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class DefaultFilter extends AbstractFilter {

    public DefaultFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryCreator bentoParam) {
        return new SearchSourceBuilder()
                .query(
                        bentoParam.getQuery()
                );
    }
}
