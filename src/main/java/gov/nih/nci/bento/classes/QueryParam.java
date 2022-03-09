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
import java.util.Set;

@Getter
public class QueryParam {

    private final Map<String, Object> args;
    private final Map<String, String> returnTypes;
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
        this.orderBy = getOrderByText(args);
    }

    private static String getOrderByText(Map<String, Object> args) {
        String orderBy = args.containsKey(Const.ES_PARAMS.ORDER_BY) ? (String) args.get(Const.ES_PARAMS.ORDER_BY) : "";
        return orderBy + addKeywordType(orderBy);
    }

    // TODO Check Better Way
    private static String addKeywordType(String param) {
        Set<String> nonKeyword = Set.of(
                Const.BENTO_FIELDS.FILE_ID_NUM,
                Const.BENTO_FIELDS.SUBJECT_ID_NUM,
                Const.BENTO_FIELDS.SURVIVAL_TIME,
                Const.BENTO_FIELDS.AGE_AT_INDEX,
                Const.BENTO_FIELDS.AGE);
        return nonKeyword.contains(param) ? "" : Const.ES_UNITS.KEYWORD;
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

            if (e instanceof GraphQLObjectType) {
                GraphQLObjectType type = (GraphQLObjectType) e;
                List<GraphQLFieldDefinition> lists = type.getFieldDefinitions();
                lists.forEach(field -> result.put(field.getName(), field.getName()));
                // TODO
            } else if (e instanceof GraphQLFieldDefinition){
                GraphQLFieldDefinition field = (GraphQLFieldDefinition) e;
                result.put(field.getName(), field.getType().toString());
            }
        });
        return result;
    }

}
