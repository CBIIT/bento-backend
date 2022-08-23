package gov.nih.nci.bento.search.query;

import gov.nih.nci.bento.classes.FilterParam;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;

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
                    QueryBuilder builder = filterParam.isCaseInsensitive() ? getCaseInsensitiveQuery(List.of(val), key) : QueryBuilders.termQuery(key, args.get(key));
                    boolBuilder.filter(builder);
                }
            } else {
                List<String> list = (List<String>) args.get(key);
                // Skip to Filter Nested Fields
                if (list.size() > 0 && !filterParam.getNestedParameters().contains(key)) {
                    QueryBuilder builder = filterParam.isCaseInsensitive() ? getCaseInsensitiveQuery(list, key) : QueryBuilders.termsQuery(key, (List<String>) args.get(key));
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