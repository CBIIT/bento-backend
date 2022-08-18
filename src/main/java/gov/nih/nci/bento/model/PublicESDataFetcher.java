package gov.nih.nci.bento.model;

import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class PublicESDataFetcher extends AbstractESDataFetcher {
    private static final Logger logger = LogManager.getLogger(PublicESDataFetcher.class);

    public PublicESDataFetcher(ESService esService) {
        super(esService);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("publicGlobalSearch", env -> {
                            Map<String, Object> args = env.getArguments();
                            return globalSearch(args);
                        })
                )
                .build();
    }
}
