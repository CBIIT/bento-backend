package gov.nih.nci.bento.model.search.query;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;

import static gov.nih.nci.bento.constants.Const.getTempQueryParamMap;

public class BentoQueryCreator extends QueryCreator {

    public BentoQueryCreator(Map<String, Object> args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public QueryBuilder createQuery(Map<String, Object> args, BoolQueryBuilder boolBuilder) {
        Map<String, String> keyMap= getTempQueryParamMap();
        args.forEach((k,v)->{
            List<String> list = (List<String>) args.get(k);
            if (list.size() > 0) {
                QueryBuilder builder = QueryBuilders.termsQuery(keyMap.getOrDefault(k, k), (List<String>) args.get(k));
                boolBuilder.filter(builder);
            }
        });
        return boolBuilder.filter().size() > 0 ? boolBuilder : QueryBuilders.matchAllQuery();
    }
}
