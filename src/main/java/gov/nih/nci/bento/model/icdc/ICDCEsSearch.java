package gov.nih.nci.bento.model.icdc;

import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.search.result.TypeMapperService;
import gov.nih.nci.bento.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.service.EsSearch;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;


@RequiredArgsConstructor
public class ICDCEsSearch implements DataFetcher {
    private static final Logger logger = LogManager.getLogger(ICDCEsSearch.class);
    private final EsSearch esService;

    public final TypeMapperService typeMapper;
//    private BentoQuery bentoQuery;
    private YamlQueryFactory yamlQueryFactory;

    @PostConstruct
    public void init() {
        yamlQueryFactory = new YamlQueryFactory(esService, typeMapper);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                                .dataFetchers(yamlQueryFactory.createICDCYamlQueries())
                )
                .build();
    }
}
