package gov.nih.nci.bento.search.query;

import gov.nih.nci.bento.classes.FilterParam;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.*;

public abstract class QueryFactory {
    // Range Query Has Different Query Option
    private final Set<String> rangeFields;
    private Map<String, Object> args;
    public QueryFactory(Map<String, Object> args, FilterParam param, Set<String> ranges) {
        this.args = args;
        this.rangeFields = ranges;
    }

    public QueryBuilder getQuery() {
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        // Create Range Query
        rangeFields.forEach(range->{
            @SuppressWarnings("unchecked")
            List<String> list = args.containsKey(range) ? (List<String>) args.get(range) : new ArrayList<>();
            if (list.size() > 0) boolBuilder.filter(getRangeType(range, list));
            args.remove(range);
        });
        return createQuery(args, boolBuilder);
    }

    private QueryBuilder getRangeType(String field, List<String> strList) {
        return QueryBuilders.rangeQuery(field)
                .gte(strList.get(0))
                .lte(strList.get(1));
    }

    abstract QueryBuilder createQuery(Map<String, Object> args, BoolQueryBuilder boolBuilder);
}