package gov.nih.nci.bento.service;

import gov.nih.nci.bento.error.ApiError;
import gov.nih.nci.bento.model.ConfigurationDAO;
import graphql.schema.GraphQLSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphql.Cypher;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Component to initialize the GraphQL to Cypher translator
 */
@Component
public class TranslationProvider {
    @Autowired
    private ConfigurationDAO config;

    private static final Logger logger = LogManager.getLogger(TranslationProvider.class);
    private Translator translator;

    /**
     * Reads the schema file and builds the translator object
     * @throws IOException
     */
    @PostConstruct
    public void init() throws IOException{
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + config.getSchemaFile());
        File schemaFile = resource.getFile();
        String schemaString = Files.readString(schemaFile.toPath());

        GraphQLSchema schema = SchemaBuilder.buildSchema(schemaString);
        translator = new Translator(schema);
    }

    /**
     * Translates the supplied GraphQL operation into a Cypher object
     * @param graphQL The graphQL to be translated
     * @return The Cypher object containing the translation
     * @throws ApiError
     */
    public List<Cypher> translateToCypher(String graphQL) throws ApiError {
        try{
            return translator.translate(graphQL);
        }
        catch (Exception e){
            throw new ApiError(HttpStatus.BAD_REQUEST, "Exception occurred while translating query", e.toString());
        }
    }
}
