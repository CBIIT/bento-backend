package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class DefaultFilter extends AbstractFilter {

    public DefaultFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam, boolean loadAllData) {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(loadAllData ? QueryBuilders.matchAllQuery() : bentoParam.getQuery());
        if (param.getSize()!=0) builder.size(param.getSize());
        if (loadAllData) builder.size(Const.ES_UNITS.MAX_SIZE);
        return builder;
    }
}
