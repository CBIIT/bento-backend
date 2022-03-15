package gov.nih.nci.bento.model.search.query;

import gov.nih.nci.bento.classes.FilterParam;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class SearchCountFilter extends AbstractFilter {

    public SearchCountFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryCreator bentoParam) {
        return new SearchSourceBuilder()
                .size(0)
                .query(bentoParam.getQuery());
    }
}
