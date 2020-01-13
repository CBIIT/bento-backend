package gov.nih.nci.icdc.service;



import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import gov.nih.nci.icdc.model.ConfigurationDAO;
import graphql.GraphQL;
import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.validation.ValidationError;
import graphql.validation.Validator;

/**
 * This class initialize GraphQL object. 
 * Step1:  define schema
 * Step2:  define how to interoperate with schema through data fetchers. Data fetchers will fetch the data and  wiring the data with schema.
 * 
 */

@Component
public class GraphQLProvider {


    private GraphQL graphQL;
    
    private GraphQLSchema schema;
    
    @Autowired
	private ConfigurationDAO config;

    
    @PostConstruct
    public void init() throws IOException {
        String sdl =config.getGraphSchemas();
        this.schema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(this.schema).build();
    }
    

	/**
	   * Make a executable schema.
	   * Step1 : generate a schema from plain schema string
	   * Step2: make runting wiring
	   * Step3: put step1 and stpe2's result together. 
	   * 
	   */
    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    
    public boolean isVaild(Document document) {
    	Validator validator = new Validator();
        List<ValidationError> validationErrors = validator.validateDocument(this.schema, document);
    	return validationErrors.size() == 0;
    }
    
    /**
	   * Make runtime wiring
	   * 
	   */
    private RuntimeWiring buildWiring() {
         RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring().build();
         return runtimeWiring;
    }

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }
    
}