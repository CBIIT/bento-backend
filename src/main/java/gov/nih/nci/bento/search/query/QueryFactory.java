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
    private final FilterParam filterParam;

    public QueryFactory(Map<String, Object> args, FilterParam param) {
        this.filterParam = param;
        this.args = args;
        this.rangeFields = param.isRangeFilter() ? Set.of(param.getSelectedField()) : new HashSet<>();
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
        // TODO
//        filterParam.getNestedFields().forEach((nestedField)->{
//            args.remove(nestedField);
//        });
        return createQuery(args, boolBuilder);
    }
    // TODO
    public QueryBuilder getNestedQuery() {

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (filterParam.isNestedFilter()) {
            // Get Arguments
            // Check if nested fields included
            Set<String> nestedFields = filterParam.getNestedFields() !=null ? filterParam.getNestedFields() : new HashSet<>();
            filterParam.getArgs().forEach((k,v)->{
                if (nestedFields.contains(k)) {
                    List<String> list = filterParam.getArgs().containsKey(k) ? (List<String>) filterParam.getArgs().get(k) : new ArrayList<>();
                    if (list.size() > 0) {
                        list.forEach(l->{
                            boolQueryBuilder.should(QueryBuilders.termQuery(filterParam.getNestedPath() + "." + k, l));
                        });
                    }
                }
            });
        }
        return boolQueryBuilder;
    }

    private QueryBuilder getRangeType(String field, List<String> strList) {
        return QueryBuilders.rangeQuery(field)
                .gte(strList.get(0))
                .lte(strList.get(1));
    }

    abstract QueryBuilder createQuery(Map<String, Object> args, BoolQueryBuilder boolBuilder);
}
