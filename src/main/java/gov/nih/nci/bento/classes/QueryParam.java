package gov.nih.nci.bento.classes;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.utility.ElasticUtil;
import graphql.schema.*;
import lombok.Builder;
import lombok.Getter;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;

@Getter
public class QueryParam {

    private final Map<String, Object> args;
    private final Set<String> returnTypes;
    // Store PageSize, Offset, Sort
    private final TableParam tableParam;
    private final String searchText;

    @Builder
    @SuppressWarnings("unchecked")
    public QueryParam(Map<String, Object> args, GraphQLOutputType outputType) {
        this.args = args;
        this.returnTypes = getReturnType(outputType);
        this.tableParam = setTableParam(args);
        this.searchText = args.containsKey(Const.ES_PARAMS.INPUT) ?  (String) args.get(Const.ES_PARAMS.INPUT) : "";
    }

    private TableParam setTableParam(Map<String, Object> args) {
        return TableParam.builder()
                .offSet(args.containsKey(Const.ES_PARAMS.OFFSET) ?  (int) args.get(Const.ES_PARAMS.OFFSET) : -1)
                .pageSize(args.containsKey(Const.ES_PARAMS.PAGE_SIZE) ?  (int) args.get(Const.ES_PARAMS.PAGE_SIZE) : -1)
                .orderBy(getOrderByText(args))
                .sortDirection(getSortType())
                .build();
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

    private Set<String> getReturnType(GraphQLOutputType outputType) {
        Set<String> result = new HashSet<>();
        SchemaElementChildrenContainer container = outputType.getChildrenWithTypeReferences();
        // TODO Only one
        List<GraphQLSchemaElement> elements = container.getChildrenAsList();
        elements.forEach((e) -> {

            if (e instanceof GraphQLObjectType) {
                GraphQLObjectType type = (GraphQLObjectType) e;
                List<GraphQLFieldDefinition> lists = type.getFieldDefinitions();
                lists.forEach(field -> result.add(field.getName()));
                // TODO
            } else if (e instanceof GraphQLFieldDefinition){
                GraphQLFieldDefinition field = (GraphQLFieldDefinition) e;
                result.add(field.getName());
            }
        });
        return result;
    }

}
