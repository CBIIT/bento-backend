package gov.nih.nci.bento.model.search.query;

import gov.nih.nci.bento.classes.FilterParam;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;
import java.util.Map;

import static gov.nih.nci.bento.constants.Const.getTempQueryParamMap;

public class RangeFilter extends AbstractFilter {


    public RangeFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilterTest(FilterParam param, Map<String, Object> args) {
        QueryBuilder query = createQuery(param.getSelectedField(), args);
        return new SearchSourceBuilder()
                .size(0)
                .query(query)
                .aggregation(AggregationBuilders
                        .max("max").field(param.getSelectedField()))
                .aggregation(AggregationBuilders
                        .min("min").field(param.getSelectedField()));
    }

    // TODO
    private QueryBuilder getRangeType(String field, List<String> strList) {
        return QueryBuilders.rangeQuery(field)
                .gte(strList.get(0))
                .lte(strList.get(1));
    }

    // TODO DELETED Custom Map
    private QueryBuilder createQuery(String field, Map<String, Object> args) {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        Map<String, String> keyMap= getTempQueryParamMap();
        args.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);
            if (list.size() > 0) {
                QueryBuilder builder =QueryBuilders.termsQuery(keyMap.getOrDefault(k, k), (List<String>) args.get(k));
                // Set Range Query Or Default Term Query
                if (field.equals(k)) builder = getRangeType(keyMap.getOrDefault(k, k), list);
                bool.filter(builder);
            }
        });
        return bool.filter().size() > 0 ? bool : QueryBuilders.matchAllQuery();
    }
}
