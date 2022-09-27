package gov.nih.nci.bento.model.search.query;

import gov.nih.nci.bento.model.search.filter.FilterParam;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryFactory {
    private FilterParam filterParam;
    public QueryFactory(FilterParam param) {
        this.filterParam = param;
    }

    public QueryBuilder getQuery() {
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        Map<String, Object> args = new HashMap<>(filterParam.getArgs());
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            String key = entry.getKey();
            List<String> list = (List<String>) args.get(key);
            if (list.size() > 0 && filterParam.isCaseInsensitive()) {
                boolBuilder.filter(getCaseInsensitiveQuery(list, key));
                continue;
            }
            if (list.size() > 0) boolBuilder.filter(QueryBuilders.termsQuery(key, (List<String>) args.get(key)));
        }
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
