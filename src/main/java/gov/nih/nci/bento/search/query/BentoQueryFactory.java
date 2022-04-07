package gov.nih.nci.bento.search.query;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;

public class BentoQueryFactory extends QueryFactory {

    public BentoQueryFactory(Map<String, Object> args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public QueryBuilder createQuery(Map<String, Object> args, BoolQueryBuilder boolBuilder) {
        args.forEach((key,v)->{
            List<String> list = (List<String>) args.get(key);
            if (list.size() > 0) {
                QueryBuilder builder = QueryBuilders.termsQuery(key, (List<String>) args.get(key));
                boolBuilder.filter(builder);
            }
        });
        return boolBuilder.filter().size() > 0 ? boolBuilder : QueryBuilders.matchAllQuery();
    }
}
