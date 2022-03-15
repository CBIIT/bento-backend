package gov.nih.nci.bento.search.datafetcher;

import graphql.schema.idl.RuntimeWiring;

public interface DataFetcher {
    RuntimeWiring buildRuntimeWiring();
}
