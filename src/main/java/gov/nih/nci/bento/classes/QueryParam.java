package gov.nih.nci.bento.classes;

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

    @Builder
    public QueryParam(Map<String, Object> args, GraphQLOutputType outputType) {
        this.args = args;
        this.returnTypes = getReturnType(outputType);
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
