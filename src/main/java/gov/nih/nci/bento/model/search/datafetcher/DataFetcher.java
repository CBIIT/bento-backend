package gov.nih.nci.bento.model.search.datafetcher;

import graphql.schema.idl.RuntimeWiring;

public interface DataFetcher {
    RuntimeWiring buildRuntimeWiring();
}
