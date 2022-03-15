package gov.nih.nci.bento.search;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.search.datafetcher.Neo4jDataFetcher;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphql.SchemaBuilder;
import org.neo4j.graphql.SchemaConfig;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class GraphQLBuilder {

    private final ConfigurationDAO config;
    private final Neo4jDataFetcher dataFetcherInterceptor;
    private final DataFetcher esFilterDataFetcher;

    private Gson gson = new GsonBuilder().serializeNulls().create();
    private GraphQL graphql;

    @PostConstruct
    public void initGraphQL() throws IOException {
        GraphQLSchema neo4jSchema = getNeo4jSchema();
        GraphQLSchema esSchema = getEsSchema();
        GraphQLSchema newSchema = mergeSchema(neo4jSchema, esSchema);
        graphql = GraphQL.newGraphQL(newSchema).build();
    }

    private GraphQLSchema getEsSchema() throws IOException {
        if (config.isEsFilterEnabled()){
            File schemaFile = new DefaultResourceLoader().getResource("classpath:" + config.getEsSchemaFile()).getFile();
            return new SchemaGenerator().makeExecutableSchema(new SchemaParser().parse(schemaFile), esFilterDataFetcher.buildRuntimeWiring());
        }
        return null;
    }

    @NotNull
    private GraphQLSchema getNeo4jSchema() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:" + config.getSchemaFile());
        File schemaFile = resource.getFile();
        String schemaString = Files.readString(schemaFile.toPath());
        SchemaConfig schemaConfig = new SchemaConfig();

        return SchemaBuilder.buildSchema(schemaString, schemaConfig, dataFetcherInterceptor);
    }


    private GraphQLSchema mergeSchema(GraphQLSchema schema1, GraphQLSchema schema2) {
        if (schema1 == null) return schema2;
        if (schema2 == null) return schema1;

        var builder = GraphQLSchema.newSchema(schema1);
        var codeRegistry2 = schema2.getCodeRegistry();
        builder.codeRegistry(schema1.getCodeRegistry().transform( crBuilder -> {crBuilder.dataFetchers(codeRegistry2); crBuilder.typeResolvers(codeRegistry2);}));
        var allTypes = new HashMap<String, GraphQLNamedType>(schema1.getTypeMap());
        allTypes.putAll(schema2.getTypeMap());

        //Remove individual schema query, mutation, and subscription types from all types to prevent naming conflicts
        removeQueryMutationSubscription(allTypes, Arrays.asList(schema1, schema2));

        //Add merged query, mutation, and subscription types
        GraphQLNamedType mergedQuery = mergeType(schema1.getQueryType(), schema2.getQueryType());
        if (mergedQuery != null) allTypes.put(Const.GRAPHQL.QUERY_TYPE_NAME, mergedQuery);

        GraphQLNamedType mergedMutation = mergeType(schema1.getMutationType(), schema2.getMutationType());
        if (mergedMutation != null)	allTypes.put(Const.GRAPHQL.MUTATION_TYPE_NAME, mergedMutation);

        GraphQLNamedType mergedSubscription = mergeType(schema1.getSubscriptionType(), schema2.getSubscriptionType());
        if (mergedSubscription != null) allTypes.put(Const.GRAPHQL.SUBSCRIPTION_TYPE_NAME, mergedSubscription);

        builder.query((GraphQLObjectType) allTypes.get(Const.GRAPHQL.QUERY_TYPE_NAME));
        builder.mutation((GraphQLObjectType) allTypes.get(Const.GRAPHQL.MUTATION_TYPE_NAME));
        builder.subscription((GraphQLObjectType) allTypes.get(Const.GRAPHQL.SUBSCRIPTION_TYPE_NAME));

        builder.clearAdditionalTypes();
        allTypes.values().forEach(builder::additionalType);

        return builder.build();
    }

    private void removeQueryMutationSubscription(
            HashMap<String, GraphQLNamedType> allTypes, List<GraphQLSchema> schemas){

        schemas.forEach((schema)->{
            try {
                String name = schema.getQueryType().getName();
                allTypes.remove(name);
            }
            catch (NullPointerException e){}

            try {
                String name = schema.getMutationType().getName();
                allTypes.remove(name);
            }
            catch (NullPointerException e){}

            try {
                String name = schema.getSubscriptionType().getName();
                allTypes.remove(name);
            }
            catch (NullPointerException e){}
        });
    }

    private GraphQLNamedType mergeType(GraphQLObjectType type1, GraphQLObjectType type2) {
        if (type1 == null) return type2;
        if (type2 == null) return type1;

        var builder = GraphQLObjectType.newObject(type1);
        type2.getFieldDefinitions().forEach(builder::field);
        return builder.build();
    }

    public String query(String sdl, Map<String, Object> variables) {
        ExecutionInput.Builder builder = ExecutionInput.newExecutionInput().query(sdl);
        if (variables != null) builder = builder.variables(variables);

        ExecutionInput input = builder.build();
        ExecutionResult executionResult = graphql.execute(input);
        Map<String, Object> standardResult = executionResult.toSpecification();
        return gson.toJson(standardResult);
    }

}
