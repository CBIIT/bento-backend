package gov.nih.nci.bento.classes;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.utility.ElasticUtil;
import graphql.schema.*;
import lombok.Builder;
import lombok.Getter;
import org.opensearch.search.sort.SortOrder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class QueryParam {
    private final Map<String, Object> args;
    private final Set<String> returnTypes;
    private final Set<String> globalSearchResultTypes;
    // Store PageSize, Offset, Sort
    private final TableParam tableParam;
    private final String searchText;

    @Builder
    public QueryParam(Map<String, Object> args, GraphQLOutputType outputType) {
        ReturnType returnType = getReturnType(outputType);
        this.args = args;
        this.returnTypes = returnType.defaultSet;
        this.globalSearchResultTypes = returnType.globalSet;
        this.tableParam = setTableParam(args);
        this.searchText = args.containsKey(Const.ES_PARAMS.INPUT) ?  (String) args.get(Const.ES_PARAMS.INPUT) : "";
    }

    @Getter
    private static class ReturnType {
        private final Set<String> defaultSet;
        private final Set<String> globalSet;
        @Builder
        protected ReturnType(Set<String> defaultSet, Set<String> globalSet) {
            this.defaultSet = defaultSet;
            this.globalSet = globalSet;
        }
    }

    private TableParam setTableParam(Map<String, Object> args) {
        return TableParam.builder()
                .offSet(args.containsKey(Const.ES_PARAMS.OFFSET) ?  (int) args.get(Const.ES_PARAMS.OFFSET) : -1)
                .pageSize(getPageSize(args))
                .orderBy(getOrderByText(args))
                .sortDirection(getSortType())
                .build();
    }

    private int getPageSize(Map<String, Object> args) {
        if (!args.containsKey(Const.ES_PARAMS.PAGE_SIZE)) return -1;
        return Math.min((int) args.get(Const.ES_PARAMS.PAGE_SIZE), Const.ES_UNITS.MAX_SIZE);
    }

    private String getOrderByText(Map<String, Object> args) {
        return args.containsKey(Const.ES_PARAMS.ORDER_BY) ? (String) args.get(Const.ES_PARAMS.ORDER_BY) : "";
    }

    private SortOrder getSortType() {
        if (args.containsKey(Const.ES_PARAMS.SORT_DIRECTION))
            return ElasticUtil.getSortType((String) args.get(Const.ES_PARAMS.SORT_DIRECTION));
        return SortOrder.DESC;
    }

    private ReturnType getReturnType(GraphQLOutputType outputType) {
        Set<String> defaultSet = new HashSet<>();
        Set<String> globalSearchSet = new HashSet<>();
        SchemaElementChildrenContainer container = outputType.getChildrenWithTypeReferences();
        // TODO Only one
        List<GraphQLSchemaElement> elements = container.getChildrenAsList();
        elements.forEach((e) -> {

            if (e instanceof GraphQLObjectType) {
                GraphQLObjectType type = (GraphQLObjectType) e;
                List<GraphQLFieldDefinition> lists = type.getFieldDefinitions();
                lists.forEach(field -> defaultSet.add(field.getName()));
                // TODO
            } else if (e instanceof GraphQLFieldDefinition){
                GraphQLObjectType graphQLObjectType = (GraphQLObjectType) outputType;
                if (graphQLObjectType.getName().contains("GlobalSearch") && ((GraphQLFieldDefinition) e).getName().equals("result")) {
                    e.getChildren().forEach(c->{
                        SchemaElementChildrenContainer container1 = c.getChildrenWithTypeReferences();
                        List<?> fieldTypes = container1.getChildren("wrappedType");
                        fieldTypes.forEach(fileType->{
                            GraphQLObjectType graphQLType = (GraphQLObjectType) fileType;
                            List<GraphQLFieldDefinition> graphQLFieldDefinitionList = graphQLType.getFieldDefinitions();
                            graphQLFieldDefinitionList.forEach(g->globalSearchSet.add(g.getName()));
                        });
                    });
                }
                GraphQLFieldDefinition field = (GraphQLFieldDefinition) e;
                defaultSet.add(field.getName());
            }
        });

        return ReturnType.builder()
                .defaultSet(defaultSet)
                .globalSet(globalSearchSet)
                .build();
    }
}
