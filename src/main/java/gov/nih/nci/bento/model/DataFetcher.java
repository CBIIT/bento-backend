package gov.nih.nci.bento.model;

import graphql.schema.idl.RuntimeWiring;

public interface DataFetcher {
    RuntimeWiring buildRuntimeWiring();
}
