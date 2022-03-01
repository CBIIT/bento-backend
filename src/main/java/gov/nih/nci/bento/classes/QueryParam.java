package gov.nih.nci.bento.classes;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.utility.ElasticUtil;
import graphql.schema.*;
import lombok.Builder;
import lombok.Getter;
import org.elasticsearch.search.sort.SortOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class QueryParam {

    private Map<String, Object> args;
    private Map<String, String> returnTypes;
    private final int pageSize;
    private final int offSet;
    private final SortOrder sortDirection;
    private final String orderBy;


    @Builder
    @SuppressWarnings("unchecked")
    public QueryParam(Map<String, Object> args, GraphQLOutputType outputType) {
        this.args = args;
        this.returnTypes = getReturnType(outputType);
        this.pageSize = args.containsKey(Const.ES_PARAMS.PAGE_SIZE) ?  (int) args.get(Const.ES_PARAMS.PAGE_SIZE) : -1;
        this.offSet = args.containsKey(Const.ES_PARAMS.OFFSET) ?  (int) args.get(Const.ES_PARAMS.OFFSET) : -1;
        this.sortDirection = getSortType();
        this.orderBy = args.containsKey(Const.ES_PARAMS.OFFSET) ? (String) args.get(Const.ES_PARAMS.ORDER_BY) : "";
    }

    private SortOrder getSortType() {
        if (args.containsKey(Const.ES_PARAMS.SORT_DIRECTION))
            return ElasticUtil.getSortType((String) args.get(Const.ES_PARAMS.SORT_DIRECTION));
        return SortOrder.DESC;
    }

    private Map<String, String> getReturnType(GraphQLOutputType outputType) {
        Map<String, String> result = new HashMap<>();
        SchemaElementChildrenContainer container = outputType.getChildrenWithTypeReferences();
        // TODO Only one
        List<GraphQLSchemaElement> elements = container.getChildrenAsList();
        elements.forEach((e) -> {
            GraphQLObjectType type = (GraphQLObjectType) e;
            List<GraphQLFieldDefinition> lists = type.getFieldDefinitions();
            lists.forEach(field -> {
                result.put(field.getName(), field.getName());
            });
        });
        return result;
    }

}
