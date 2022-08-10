package gov.nih.nci.bento.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * The Configuration Bean, reads configuration setting from classpath:application.properties.
 */
@Configuration
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
@Getter
public class ConfigurationDAO {
	private static final Logger logger = LogManager.getLogger(ConfigurationDAO.class);

	//Bento API Version
	@Value("${bento.api.version}")
	private String bentoApiVersion;
	//Project
	@Value("${project:bento}")
	private String project;

	//Enable authentication check
	@Value("${auth.enabled}")
	private boolean authEnabled;
	@Value("${auth_endpoint:}")
	private String authEndpoint;

	//Neo4j Connection
	@Value("${neo4j.url}")
	private String neo4jUrl;
	@Value("${neo4j.user}")
	private String neo4jUser;
	@Value("${neo4j.password}")
	private String neo4jPassword;

	//Private GraphQL Schemas
	@Value("${graphql.schema}")
	private String schemaFile;
	@Value("${graphql.es_schema}")
	private String esSchemaFile;

	//Public Graphql Schemas
	@Value("${graphql.public.schema}")
	private String publicSchemaFile;
	@Value("${graphql.public.es_schema}")
	private String publicEsSchemaFile;

	//Operation Type Enable
	@Value("${allow_graphql_query}")
	private boolean allowGraphQLQuery;
	@Value("${allow_graphql_mutation}")
	private boolean allowGraphQLMutation;

	//Redis Cache Configuration
	@Value("${redis.enable}")
	private boolean redisEnabled;
	@Value("${redis.use_cluster}")
	private boolean redisUseCluster;
	@Value("${redis.host}")
	private String redisHost;
	@Value("${redis.port}")
	private int redisPort;
	@Value("${redis.ttl}")
	private int redisTTL;

	//Elasticsearch Configuration
	@Value("${es.host}")
	private String esHost;
	@Value("${es.port}")
	private int esPort;
	@Value("${es.scheme}")
	private String esScheme;
	@Value(("${es.filter.enabled}"))
	private boolean esFilterEnabled;
	@Value("${es.sign.requests:true}")
	private boolean esSignRequests;

	//Testing
	@Value("${test.queries_file}")
	private String testQueriesFile;
}
