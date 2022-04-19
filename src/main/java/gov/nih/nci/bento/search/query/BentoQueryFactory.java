package gov.nih.nci.bento.search.query;

import gov.nih.nci.bento.classes.FilterParam;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BentoQueryFactory extends QueryFactory {

    private FilterParam filterParam;

    public BentoQueryFactory(Map<String, Object> args, FilterParam filterParam) {
        super(args);
        this.filterParam = filterParam;
    }

    @Override
    @SuppressWarnings("unchecked")
    public QueryBuilder createQuery(Map<String, Object> args, BoolQueryBuilder boolBuilder) {
        args.forEach((key,v)->{
            // TODO
            if (args.get(key) instanceof String) {
                String val = (String) args.get(key);
                if (!val.equals("")) {
                    QueryBuilder builder = filterParam.isCaseInSensitive() ? getCaseInsensitiveQuery(List.of(val), key) : QueryBuilders.termQuery(key, args.get(key));
                    boolBuilder.filter(builder);
                }
            } else {
                List<String> list = (List<String>) args.get(key);
                // Skip to Filter Nested Fields
                Optional<String> nestedPath = Optional.ofNullable(filterParam.getNestedPath());
                if (list.size() > 0 && nestedPath.isEmpty()) {
                    QueryBuilder builder = filterParam.isCaseInSensitive() ? getCaseInsensitiveQuery(list, key) : QueryBuilders.termsQuery(key, (List<String>) args.get(key));
                    boolBuilder.filter(builder);
                }
            }
        });
        return boolBuilder.filter().size() > 0 ? boolBuilder : QueryBuilders.matchAllQuery();
    }

    private QueryBuilder getCaseInsensitiveQuery(List<String> list, String key) {
        BoolQueryBuilder bool = new BoolQueryBuilder();
        list.forEach(value->
            bool.should(
                    QueryBuilders.wildcardQuery(key, value).caseInsensitive(true)
            )
        );
        return bool;
    }

}
