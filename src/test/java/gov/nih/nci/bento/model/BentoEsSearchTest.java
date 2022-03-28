package gov.nih.nci.bento.model;

import gov.nih.nci.bento.config.ConfigurationDAO;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;
@RunWith( SpringRunner.class )
@SpringBootTest
public class BentoEsSearchTest {

    @Test
    public void bentoWiringTest() throws IOException {
        buildRuntimeWiring();
        System.out.println("");

    }

    @Autowired
    ConfigurationDAO config;

    public int setHi(Object env) {
        return 1;

    }

    public void buildRuntimeWiring() throws IOException {


        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                                .dataFetcher("searchSubjects", env -> setHi(env))
                )
                .build();


        String str = config.getEsSchemaFile();
        File schemaFile = new DefaultResourceLoader().getResource("classpath:" + config.getEsSchemaFile()).getFile();
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(
                new SchemaParser().parse(schemaFile),
                runtimeWiring
        );
        graphQLSchema.getQueryType().getFieldDefinitions();
        System.out.println("");
    }
//
//    private GraphQLSchema getEsSchema() throws IOException {
//        if (config.isEsFilterEnabled()){
//            File schemaFile = new DefaultResourceLoader().getResource("classpath:" + config.getEsSchemaFile()).getFile();
//            return new SchemaGenerator().makeExecutableSchema(new SchemaParser().parse(schemaFile), esFilterDataFetcher.buildRuntimeWiring());
//        }
//        return null;
//    }

}