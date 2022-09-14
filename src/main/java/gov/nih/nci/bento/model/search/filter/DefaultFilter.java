package gov.nih.nci.bento.model.search.filter;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.search.query.QueryFactory;
import org.opensearch.search.builder.SearchSourceBuilder;

public class DefaultFilter extends AbstractFilter {

    public DefaultFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam) {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(bentoParam.getQuery());
        builder.size(Const.ES_UNITS.MAX_SIZE);
        return builder;
    }
}
