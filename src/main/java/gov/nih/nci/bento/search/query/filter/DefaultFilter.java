package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.*;

public class DefaultFilter extends AbstractFilter {

    public DefaultFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam) {
        // Exception Case; If all list contains empty string, give all documents
        if (isAllContainEmptyString(param))
            return new SearchSourceBuilder()
                    .query(QueryBuilders.matchAllQuery())
                    .size(Const.ES_UNITS.MAX_SIZE);

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(bentoParam.getQuery());

        if (param.getSize()!=0) builder.size(param.getSize());

        return builder;
    }

    private boolean isAllContainEmptyString(FilterParam param) {
        Map<String, Object> args = param.getArgs();
        Set<String> conditionFieldSet = param.getReturnAllFields();
        boolean fieldMatchCondition = conditionFieldSet.size() > 0 && conditionFieldSet.stream().allMatch(item->args.containsKey(item));
        List<List<String>> values = new ArrayList<>();
        args.forEach((k, v) -> {
            if (conditionFieldSet.contains(k)) values.add((List<String>) args.get(k));
        });
        boolean emptyStrCondition = values.size() > 0 && values.stream().allMatch(item->item.size()==1 && item.contains(""));
        return fieldMatchCondition && emptyStrCondition;
    }

}
