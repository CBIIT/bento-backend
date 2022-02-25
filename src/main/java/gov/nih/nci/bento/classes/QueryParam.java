package gov.nih.nci.bento.classes;

import gov.nih.nci.bento.constants.Const;
import graphql.schema.*;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class QueryParam {

    private Map<String, Object> args;
    private Map<String, String> returnTypes;
    private int pageSize;
    private int offSet;

    @Builder
    public QueryParam(Map<String, Object> args, GraphQLOutputType outputType) {
        this.args = args;
        this.returnTypes = getReturnType(outputType);
        this.pageSize = args.containsKey(Const.ES_PARAMS.PAGE_SIZE) ?  (int) args.get(Const.ES_PARAMS.PAGE_SIZE) : -1;
        this.offSet = args.containsKey(Const.ES_PARAMS.OFFSET) ?  (int) args.get(Const.ES_PARAMS.OFFSET) : -1;
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
