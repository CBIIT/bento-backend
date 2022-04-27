package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class SearchCountFilter extends AbstractFilter {

    public SearchCountFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam) {
        return new SearchSourceBuilder()
                .size(0)
                .query(bentoParam.getQuery());
    }
}
