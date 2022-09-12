package gov.nih.nci.bento.model.search.query;

import gov.nih.nci.bento.constants.Const;
import graphql.schema.*;
import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class QueryParam {
    private final Map<String, Object> args;
    private final Set<String> returnTypes;
    private final String searchText;

    @Builder
    public QueryParam(Map<String, Object> args, GraphQLOutputType outputType) {
        ReturnType returnType = getReturnType(outputType);
        this.args = args;
        this.returnTypes = returnType.fields;
        this.searchText = args.containsKey(Const.ES_PARAMS.INPUT) ?  (String) args.get(Const.ES_PARAMS.INPUT) : "";
    }

    @Getter
    private static class ReturnType {
        private final Set<String> fields;
        @Builder
        protected ReturnType(Set<String> fields) {
            this.fields = fields;
        }
    }

    private ReturnType getReturnType(GraphQLOutputType outputType) {
        Set<String> defaultSet = new HashSet<>();
        SchemaElementChildrenContainer container = outputType.getChildrenWithTypeReferences();

        List<GraphQLSchemaElement> elements = container.getChildrenAsList();
        elements.forEach((e) -> {
            GraphQLObjectType type = (GraphQLObjectType) e;
            List<GraphQLFieldDefinition> lists = type.getFieldDefinitions();
            lists.forEach(field -> defaultSet.add(field.getName()));
        });

        return ReturnType.builder()
                .fields(defaultSet)
                .build();
    }
}
