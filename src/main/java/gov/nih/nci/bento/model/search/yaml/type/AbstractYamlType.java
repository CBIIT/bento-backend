package gov.nih.nci.bento.model.search.yaml.type;

import gov.nih.nci.bento.model.search.query.QueryParam;
import gov.nih.nci.bento.model.search.yaml.IFilterType;
import gov.nih.nci.bento.model.search.yaml.ITypeQuery;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractYamlType {

    public abstract void createSearchQuery(Map<String, DataFetcher> resultMap, ITypeQuery iTypeQuery, IFilterType iFilterType) throws IOException;

    protected QueryParam createQueryParam(DataFetchingEnvironment env) {
        return QueryParam.builder()
                .args(env.getArguments())
                .outputType(env.getFieldType())
                .build();
    }
}
