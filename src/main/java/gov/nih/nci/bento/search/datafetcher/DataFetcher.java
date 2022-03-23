package gov.nih.nci.bento.search.datafetcher;

import graphql.schema.idl.RuntimeWiring;

import java.io.IOException;

public interface DataFetcher {
    RuntimeWiring buildRuntimeWiring() throws IOException;
}
