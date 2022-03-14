package gov.nih.nci.bento.model.search.query;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;
import java.util.Map;

import static gov.nih.nci.bento.constants.Const.getTempQueryParamMap;

public class AggregationFilter extends AbstractFilter {

    public AggregationFilter(FilterParam param) {
        super(param);
    }

    // TODO DELETED Custom Map
    private QueryBuilder createQuery(Map<String, Object> args) {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        Map<String, String> keyMap= getTempQueryParamMap();
        args.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);
            if (list.size() > 0) {
                bool.filter(
                        QueryBuilders.termsQuery(keyMap.getOrDefault(k, k), (List<String>) args.get(k))
                );
            }
        });
        return bool.filter().size() > 0 ? bool : QueryBuilders.matchAllQuery();
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, Map<String, Object> args) {
        // TODO
        return new SearchSourceBuilder()
                .size(0)
                .query(createQuery(args))
                .aggregation(AggregationBuilders
                        .terms(Const.ES_PARAMS.TERMS_AGGS)
                        .size(Const.ES_PARAMS.AGGS_SIZE)
                        .field(param.getSelectedField()));
    }
}
