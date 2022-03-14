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

public class SubAggregationFilter extends AbstractFilter {

    public SubAggregationFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, Map<String, Object> args) {
        return new SearchSourceBuilder()
                .query(createQuery(args))
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

    private QueryBuilder getRangeType(String field, List<String> strList) {
        return QueryBuilders.rangeQuery(field)
                .gte(strList.get(0))
                .lte(strList.get(1));
    }

    // TODO DELETED Custom Map
    private QueryBuilder createQuery(Map<String, Object> args) {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        Map<String, String> keyMap= getTempQueryParamMap();
        args.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);
            if (list.size() > 0) {
                QueryBuilder builder =QueryBuilders.termsQuery(keyMap.getOrDefault(k, k), (List<String>) args.get(k));
                // Set Range Query Or Default Term Query
                // TODO
                if (k.equals(Const.BENTO_FIELDS.AGE_AT_INDEX)) builder = getRangeType(keyMap.getOrDefault(k, k), list);
                bool.filter(builder);
            }
        });
        return bool.filter().size() > 0 ? bool : QueryBuilders.matchAllQuery();
    }
}
