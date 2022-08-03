package gov.nih.nci.bento.model;

import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class PublicESDataFetcher extends AbstractESDataFetcher {
    private static final Logger logger = LogManager.getLogger(PublicESDataFetcher.class);

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("publicGlobalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return publicGlobalSearch(args);
                        })
                )
                .build();
    }

    private Map<String, Object> publicGlobalSearch(Map<String, Object> params) throws IOException {
        return getSearchCategoriesResult(params, initPublicSearchCategories());
    }
}
