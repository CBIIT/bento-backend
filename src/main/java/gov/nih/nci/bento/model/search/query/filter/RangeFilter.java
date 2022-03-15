package gov.nih.nci.bento.model.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.model.search.query.QueryCreator;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class RangeFilter extends AbstractFilter {

    public RangeFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryCreator bentoParam) {
        return new SearchSourceBuilder()
                .size(0)
                .query(bentoParam.getQuery())
                .aggregation(AggregationBuilders
                        .max("max").field(param.getSelectedField()))
                .aggregation(AggregationBuilders
                        .min("min").field(param.getSelectedField()));
    }
}
