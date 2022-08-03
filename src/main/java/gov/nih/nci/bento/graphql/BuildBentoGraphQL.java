package gov.nih.nci.bento.graphql;

import gov.nih.nci.bento.model.AbstractESDataFetcher;
import gov.nih.nci.bento.model.PrivateNeo4jDataFetcher;
import graphql.GraphQL;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.SchemaConfig;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class BuildBentoGraphQL {

    public static GraphQL buildGraphQL(String neo4jSchemaFile, PrivateNeo4jDataFetcher privateNeo4JDataFetcher) throws IOException {
        GraphQLSchema neo4jSchema = getNeo4jSchema(neo4jSchemaFile, privateNeo4JDataFetcher);
        return GraphQL.newGraphQL(neo4jSchema).build();
    }

    public static GraphQL buildGraphQLWithES(String neo4jSchemaFile, String esSchemaFile,
            PrivateNeo4jDataFetcher privateNeo4JDataFetcher, AbstractESDataFetcher esBentoDataFetcher) throws IOException {
        GraphQLSchema neo4jSchema = getNeo4jSchema(neo4jSchemaFile, privateNeo4JDataFetcher);
        GraphQLSchema esSchema = getEsSchema(esSchemaFile, esBentoDataFetcher);
        GraphQLSchema mergedSchema = mergeSchema(neo4jSchema, esSchema);
        return GraphQL.newGraphQL(mergedSchema).build();
    }

    private static GraphQLSchema getNeo4jSchema(String schema, PrivateNeo4jDataFetcher dataFetcher) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + schema);
        File schemaFile = resource.getFile();
        String schemaString = Files.readString(schemaFile.toPath());
        SchemaConfig schemaConfig = new SchemaConfig();
        GraphQLSchema neo4jSchema = SchemaBuilder.buildSchema(schemaString, schemaConfig, dataFetcher);
        return neo4jSchema;
    }

    private static GraphQLSchema getEsSchema(String esSchema, AbstractESDataFetcher bentoDataFetcher) throws IOException {
        File schemaFile = new DefaultResourceLoader().getResource("classpath:" + esSchema).getFile();
        TypeDefinitionRegistry schemaParser = new SchemaParser().parse(schemaFile);
        return new SchemaGenerator().makeExecutableSchema(schemaParser, bentoDataFetcher.buildRuntimeWiring());
    }

    private static GraphQLSchema mergeSchema(GraphQLSchema schema1, GraphQLSchema schema2) {
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

    private static HashMap<String, GraphQLNamedType> removeQueryMutationSubscription(
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

    private static GraphQLNamedType mergeType(GraphQLObjectType type1, GraphQLObjectType type2) {
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
