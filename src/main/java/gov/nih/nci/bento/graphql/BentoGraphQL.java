package gov.nih.nci.bento.graphql;

import gov.nih.nci.bento.model.AbstractESDataFetcher;
import gov.nih.nci.bento.model.AbstractNeo4jDataFetcher;
import gov.nih.nci.bento.model.ConfigurationDAO;
import gov.nih.nci.bento.model.IcdcEsFilter;
import gov.nih.nci.bento.model.PrivateESDataFetcher;
import gov.nih.nci.bento.model.PrivateNeo4jDataFetcher;
import gov.nih.nci.bento.model.PublicESDataFetcher;
import gov.nih.nci.bento.model.PublicNeo4jDataFetcher;
import gov.nih.nci.bento.service.ESService;
import gov.nih.nci.bento.service.RedisService;
import graphql.GraphQL;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.SchemaConfig;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

@Component
public class BentoGraphQL {

    private static final Logger logger = LogManager.getLogger(BentoGraphQL.class);

    private final GraphQL privateGraphQL;
    private final GraphQL publicGraphQL;

    public BentoGraphQL(ConfigurationDAO config, ESService esService, RedisService redisService) throws IOException {
        PublicNeo4jDataFetcher publicNeo4JDataFetcher = new PublicNeo4jDataFetcher(config, redisService);
        PrivateNeo4jDataFetcher privateNeo4jDataFetcher = new PrivateNeo4jDataFetcher(config, redisService);
        AbstractESDataFetcher privateESDataFetcher;
        AbstractESDataFetcher publicESDataFetcher;

        switch (config.getProject()) {
            case "icdc":
                privateESDataFetcher = new IcdcEsFilter(esService);
                break;
            case "bento":
                privateESDataFetcher = new PrivateESDataFetcher(esService);
                break;
            default:
                logger.warn("Project \"" + config.getProject() + "\" is not supported! Use default " +
                        "PrivateESDataFetcher class");
                privateESDataFetcher = new PrivateESDataFetcher(esService);
        }

        if (config.isEsFilterEnabled()){
            switch (config.getProject()) {
                //TODO add public ICDC ES data fetcher once that class is written
                /*
                case "icdc":
                    publicESDataFetcher =  new IcdcEsFilter();
                    break;
                */
                case "bento":
                    publicESDataFetcher = new PublicESDataFetcher(esService);
                    break;
                default:
                    logger.warn("Project \"" + config.getProject() + "\" is not supported! Use default " +
                            "PublicESDataFetcher class");
                    publicESDataFetcher = new PublicESDataFetcher(esService);
            }
            this.publicGraphQL = buildGraphQLWithES(config.getPublicSchemaFile(),
                    config.getPublicEsSchemaFile(), publicNeo4JDataFetcher, publicESDataFetcher);
            this.privateGraphQL = buildGraphQLWithES(config.getSchemaFile(), config.getEsSchemaFile(),
                    privateNeo4jDataFetcher, privateESDataFetcher);
        }
        else{
            this.publicGraphQL = buildGraphQL(config.getPublicSchemaFile(), publicNeo4JDataFetcher);
            this.privateGraphQL = buildGraphQL(config.getSchemaFile(), privateNeo4jDataFetcher);
        }
    }

    public GraphQL getPublicGraphQL() {
        return publicGraphQL;
    }

    public GraphQL getPrivateGraphQL() {
        return privateGraphQL;
    }

    private GraphQL buildGraphQL(String neo4jSchemaFile, AbstractNeo4jDataFetcher neo4jDataFetcher) throws IOException {
        GraphQLSchema neo4jSchema = getNeo4jSchema(neo4jSchemaFile, neo4jDataFetcher);
        return GraphQL.newGraphQL(neo4jSchema).build();
    }

    private GraphQL buildGraphQLWithES(String neo4jSchemaFile, String esSchemaFile,
            AbstractNeo4jDataFetcher privateNeo4JDataFetcher, AbstractESDataFetcher esBentoDataFetcher) throws IOException {
        GraphQLSchema neo4jSchema = getNeo4jSchema(neo4jSchemaFile, privateNeo4JDataFetcher);
        GraphQLSchema esSchema = getEsSchema(esSchemaFile, esBentoDataFetcher);
        GraphQLSchema mergedSchema = mergeSchema(neo4jSchema, esSchema);
        return GraphQL.newGraphQL(mergedSchema).build();
    }

    private GraphQLSchema getNeo4jSchema(String schema, AbstractNeo4jDataFetcher dataFetcher) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + schema);
        File schemaFile = resource.getFile();
        String schemaString = Files.readString(schemaFile.toPath());
        SchemaConfig schemaConfig = new SchemaConfig();
        GraphQLSchema neo4jSchema = SchemaBuilder.buildSchema(schemaString, schemaConfig, dataFetcher);
        return neo4jSchema;
    }

    private GraphQLSchema getEsSchema(String esSchema, AbstractESDataFetcher bentoDataFetcher) throws IOException {
        File schemaFile = new DefaultResourceLoader().getResource("classpath:" + esSchema).getFile();
        TypeDefinitionRegistry schemaParser = new SchemaParser().parse(schemaFile);
        return new SchemaGenerator().makeExecutableSchema(schemaParser, bentoDataFetcher.buildRuntimeWiring());
    }

    private GraphQLSchema mergeSchema(GraphQLSchema schema1, GraphQLSchema schema2) {
        String QUERY_TYPE_NAME = "Query";
        String MUTATION_TYPE_NAME = "Mutation";
        String SUBSCRIPTION_TYPE_NAME = "Subscription";
        if (schema1 == null) {
            return schema2;
        }
        if (schema2 == null) {
            return schema1;
        }
        var builder = GraphQLSchema.newSchema(schema1);
        var codeRegistry2 = schema2.getCodeRegistry();
        builder.codeRegistry(schema1.getCodeRegistry().transform( crBuilder -> {crBuilder.dataFetchers(codeRegistry2);
            crBuilder.typeResolvers(codeRegistry2);}));
        var allTypes = new HashMap<String, GraphQLNamedType>(schema1.getTypeMap());
        allTypes.putAll(schema2.getTypeMap());
        //Remove individual schema query, mutation, and subscription types from all types to prevent naming conflicts
        allTypes = removeQueryMutationSubscription(allTypes, schema1);
        allTypes = removeQueryMutationSubscription(allTypes, schema2);
        //Add merged query, mutation, and subscription types
        GraphQLNamedType mergedQuery = mergeType(schema1.getQueryType(), schema2.getQueryType());
        if (mergedQuery != null){
            allTypes.put(QUERY_TYPE_NAME, mergedQuery);
        }
        GraphQLNamedType mergedMutation = mergeType(schema1.getMutationType(), schema2.getMutationType());
        if (mergedMutation != null){
            allTypes.put(MUTATION_TYPE_NAME, mergedMutation);
        }
        GraphQLNamedType mergedSubscription = mergeType(schema1.getSubscriptionType(), schema2.getSubscriptionType());
        if (mergedSubscription != null){
            allTypes.put(SUBSCRIPTION_TYPE_NAME, mergedSubscription);
        }
        builder.query((GraphQLObjectType) allTypes.get(QUERY_TYPE_NAME));
        builder.mutation((GraphQLObjectType) allTypes.get(MUTATION_TYPE_NAME));
        builder.subscription((GraphQLObjectType) allTypes.get(SUBSCRIPTION_TYPE_NAME));
        builder.clearAdditionalTypes();
        allTypes.values().forEach(builder::additionalType);
        return builder.build();
    }

    private HashMap<String, GraphQLNamedType> removeQueryMutationSubscription(
            HashMap<String, GraphQLNamedType> allTypes, GraphQLSchema schema){
        try{
            String name = schema.getQueryType().getName();
            allTypes.remove(name);
        }
        catch (NullPointerException e){}

        try{
            String name = schema.getMutationType().getName();
            allTypes.remove(name);
        }
        catch (NullPointerException e){}
        try{
            String name = schema.getSubscriptionType().getName();
            allTypes.remove(name);
        }
        catch (NullPointerException e){}
        return allTypes;
    }

    private GraphQLNamedType mergeType(GraphQLObjectType type1, GraphQLObjectType type2) {
        if (type1 == null) {
            return type2;
        }
        if (type2 == null) {
            return type1;
        }
        var builder = GraphQLObjectType.newObject(type1);
        type2.getFieldDefinitions().forEach(builder::field);
        return builder.build();
    }
}
