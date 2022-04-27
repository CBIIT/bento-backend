package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.HashSet;
import java.util.Set;

public class RangeFilter extends AbstractFilter {
    // Range Fields Globally Used to filter
    private static Set<String> rangeFields = new HashSet<>();
    public RangeFilter(FilterParam param) {
        super(param);
        rangeFields.add(param.getSelectedField());
    }

    public static Set<String> getRangeFields() {
        return new HashSet<>(rangeFields);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam) {
        return new SearchSourceBuilder()
                .size(0)
                .query(bentoParam.getQuery())
                .aggregation(AggregationBuilders
                        .max("max").field(param.getSelectedField()))
                .aggregation(AggregationBuilders
                        .min("min").field(param.getSelectedField()));
    }
}
