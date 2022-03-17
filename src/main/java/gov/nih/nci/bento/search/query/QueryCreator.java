package gov.nih.nci.bento.search.query;

import gov.nih.nci.bento.constants.Const;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class QueryCreator {
    // Range Query Has Different Query Option
    private final Set<String> rangeParams = Set.of(Const.BENTO_FIELDS.AGE_AT_INDEX);
    private Map<String, Object> args;

    public QueryCreator(Map<String, Object> args) {
        this.args = args;
    }

    public QueryBuilder getQuery() {
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        // Create Range Query
        rangeParams.forEach(range->{
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
