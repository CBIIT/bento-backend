package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class SubAggregationFilter extends AbstractFilter {

    public SubAggregationFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam, boolean loadAllData) {
        return new SearchSourceBuilder()
                .query(bentoParam.getQuery())
                .size(0)
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(param.getSelectedField())
                        .subAggregation(
                                AggregationBuilders
                                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                                        .size(Const.ES_PARAMS.AGGS_SIZE)
                                        .field(param.getSubAggSelectedField())
                        ));
    }
}
