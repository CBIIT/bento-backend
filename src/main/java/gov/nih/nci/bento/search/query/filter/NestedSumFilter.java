package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NestedSumFilter extends AbstractFilter {

    public NestedSumFilter(FilterParam param) {
        super(param);
    }

    // TODO Remove Duplicate
    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam, boolean loadAllData) {
        return new SearchSourceBuilder()
                .size(0)
                .query(bentoParam.getQuery())
                .aggregation(AggregationBuilders
                        .nested(Const.ES_PARAMS.NESTED_SEARCH, param.getNestedPath())
                        .subAggregation(
                                AggregationBuilders
                                        .filter(Const.ES_PARAMS.NESTED_FILTER, getNestedQuery(param))
                                        .subAggregation(
                                                AggregationBuilders.sum(Const.ES_PARAMS.TERMS_AGGS)
                                                        .field(param.getNestedPath() + "." + param.getSelectedField()))));
    }

    @SuppressWarnings("unchecked")
    private QueryBuilder getNestedQuery(FilterParam filterParam) {

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // Multiple nested fields
        // for the purpose of total number of aggregation & filter inner fields
        Set<String> nestedFields = new HashSet<>(filterParam.getNestedFields());
        removeFilterField(filterParam, nestedFields);
        filterParam.getArgs().forEach((k,v)->{
            if (nestedFields.contains(k)) {
                List<String> list = filterParam.getArgs().containsKey(k) ? (List<String>) filterParam.getArgs().get(k) : new ArrayList<>();
                if (list.size() > 0) {
                    list.forEach(l->
                            boolQueryBuilder.should(QueryBuilders.termQuery(filterParam.getNestedPath() + "." + k, l))
                    );
                }
            }
        });
        return boolQueryBuilder;
    }

    private void removeFilterField(FilterParam filterParam, Set<String> nestedFields) {
        if (filterParam.isExcludeFilter() && nestedFields.contains(filterParam.getSelectedField())) {
            nestedFields.remove(filterParam.getSelectedField());
        }
    }
}
