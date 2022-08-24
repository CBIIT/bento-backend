package gov.nih.nci.bento.model.search.query;

import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;

public class QueryFactory {
    private Map<String, Object> args;

    public QueryFactory(Map<String, Object> args) {
        this.args = args;
    }

    public QueryBuilder getQuery() {
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        args.forEach((key,v)->{
            List<String> list = (List<String>) args.get(key);
            if (list.size() > 0) {
                boolBuilder.filter(QueryBuilders.termsQuery(key, (List<String>) args.get(key)));
            }
        });
        return boolBuilder.filter().size() > 0 ? boolBuilder : QueryBuilders.matchAllQuery();
    }
}
