package gov.nih.nci.bento.model.bento.query;

import gov.nih.nci.bento.classes.AbstractQueryParam;
import gov.nih.nci.bento.constants.Const;
import graphql.schema.GraphQLOutputType;
import lombok.Builder;

import java.util.Map;
import java.util.Set;

public class BentoQueryParam extends AbstractQueryParam {

    @Builder
    public BentoQueryParam(Map<String, Object> args, GraphQLOutputType outputType) {
        super(args, outputType);
    }

    @Override
    public String getKeywordType(String orderBy) {
        return "";
    }

    // TODO Check Better Way
    private String addKeywordType(String param) {
        Set<String> nonKeyword = Set.of(
                Const.BENTO_FIELDS.FILE_ID_NUM,
                Const.BENTO_FIELDS.SUBJECT_ID_NUM,
                Const.BENTO_FIELDS.SURVIVAL_TIME,
                Const.BENTO_FIELDS.AGE_AT_INDEX,
                Const.BENTO_FIELDS.AGE);
        return nonKeyword.contains(param) ? "" : Const.ES_UNITS.KEYWORD;
    }

}
