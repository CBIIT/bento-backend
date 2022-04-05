package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class NestedFilter extends AbstractFilter {

    public NestedFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam) {
        return new SearchSourceBuilder()
                .size(0)
                .query(bentoParam.getQuery())
                .aggregation(AggregationBuilders
                        .nested(Const.ES_PARAMS.NESTED_SEARCH, param.getNestedPath())
                        .subAggregation(
                                AggregationBuilders
                                        .filter(Const.ES_PARAMS.NESTED_FILTER, bentoParam.getNestedQuery())
                                        .subAggregation(
                                                AggregationBuilders.terms(Const.ES_PARAMS.TERMS_AGGS)
                                                        .size(Const.ES_PARAMS.AGGS_SIZE)
                                                        .field(param.getNestedPath() + "." + param.getSelectedField()))));
    }
}
