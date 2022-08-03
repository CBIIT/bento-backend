package gov.nih.nci.bento.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * The Configuration Bean, reads configuration setting from classpath:application.properties.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class ConfigurationDAO {
	private static final Logger logger = LogManager.getLogger(ConfigurationDAO.class);

	@Autowired
	private Environment env;

	@Bean("privateESDataFetcher")
	public AbstractESDataFetcher privateESDataFetcher() {
	    String project = env.getProperty("project", "bento").toLowerCase();
	    switch (project) {
			case "icdc":
				return new IcdcEsFilter();
			case "bento":
				return new PrivateESDataFetcher();
			default:
				logger.warn("Project \"" + project + "\" is not supported! Use default PrivateESDataFetcher class");
				return new PrivateESDataFetcher();
		}
	}

	@Bean("publicESDataFetcher")
	public AbstractESDataFetcher publicESDataFetcher() {
		String project = env.getProperty("project", "bento").toLowerCase();
		switch (project) {
			//TODO add public ICDC ES data fetcher once that class is written
			/*
			case "icdc":
				return new IcdcEsFilter();
			*/
			case "bento":
				return new PublicESDataFetcher();
			default:
				logger.warn("Project \"" + project + "\" is not supported! Use default PublicESDataFetcher class");
				return new PublicESDataFetcher();
		}
	}


	//Bento API Version
	@Value("${bento.api.version}")
	private String bentoApiVersion;

	public String getBentoApiVersion() {
		return bentoApiVersion;
	}

	//Enable authentication check
	@Value("${auth.enabled}")
	private boolean authEnabled;
	@Value("${auth_endpoint:}")
	private String authEndpoint;

	public boolean getAuthEnabled() {
		return authEnabled;
	}

	public String getAuthEndpoint() {
		return authEndpoint;
	}

	//Neo4j Connection
	@Value("${neo4j.url}")
	private String neo4jUrl;
	@Value("${neo4j.user}")
	private String neo4jUser;
	@Value("${neo4j.password}")
	private String neo4jPassword;

	public String getNeo4jUrl() {
		return neo4jUrl;
	}

	public String getNeo4jUser() {
		return neo4jUser;
	}

	public String getNeo4jPassword() {
		return neo4jPassword;
	}

	//Private GraphQL Schemas
	@Value("${graphql.schema}")
	private String schemaFile;
	@Value("${graphql.es_schema}")
	private String esSchemaFile;

	public String getSchemaFile() {
		return schemaFile;
	}

	public String getEsSchemaFile() {
		return esSchemaFile;
	}

	//Public Graphql Schemas
	@Value("${graphql.public.schema}")
	private String publicSchemaFile;
	@Value("${graphql.public.es_schema}")
	private String publicEsSchemaFile;

	public String getPublicSchemaFile() {
		return publicSchemaFile;
	}

	public String getPublicEsSchemaFile() {
		return publicEsSchemaFile;
	}

	//Operation Type Enable
	@Value("${allow_graphql_query}")
	private boolean allowGraphQLQuery;
	@Value("${allow_graphql_mutation}")
	private boolean allowGraphQLMutation;

	public boolean isAllowGraphQLQuery() {
		return allowGraphQLQuery;
	}

	public boolean isAllowGraphQLMutation() {
		return allowGraphQLMutation;
	}

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

	public boolean getRedisEnabled() {
		return redisEnabled;
	}

	public boolean getRedisUseCluster() {
		return redisUseCluster;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public int getRedisPort() {
		return redisPort;
	}

	public int getRedisTTL() {
		return redisTTL;
	}

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


	public String getEsHost() {
		return esHost;
	}

	public int getEsPort() {
		return esPort;
	}

	public String getEsScheme() {
		return esScheme;
	}

	public boolean getEsFilterEnabled() {
		return esFilterEnabled;
	}

	public boolean getEsSignRequests() {
		return esSignRequests;
	}

	//Testing
	@Value("${test.queries_file}")
	private String testQueriesFile;

	public String getTestQueriesFile() {
		return testQueriesFile;
	}
}
