package gov.nih.nci.bento.model;

import com.google.gson.JsonObject;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.Request;

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
                        .dataFetcher("numberOfPrograms", env -> getNodeCount(PROGRAMS_COUNT_END_POINT))
                        .dataFetcher("numberOfStudies", env -> getNodeCount(STUDIES_COUNT_END_POINT))
                        .dataFetcher("numberOfSubjects", env -> getNodeCount(SUBJECTS_COUNT_END_POINT))
                        .dataFetcher("numberOfSamples", env -> getNodeCount(SAMPLES_COUNT_END_POINT))
                        .dataFetcher("numberOfLabProcedures", env -> getNodeCount(LAB_PROCEDURE_COUNT_END_POINT))
                        .dataFetcher("numberOfFiles", env -> getNodeCount(FILES_COUNT_END_POINT))
                )
                .build();
    }

    private int getNodeCount(String countEndpoint) throws IOException {
        Request countRequest = new Request("GET", countEndpoint);
        JsonObject countResult = esService.send(countRequest);
        return countResult.get("count").getAsInt();
    }
}
