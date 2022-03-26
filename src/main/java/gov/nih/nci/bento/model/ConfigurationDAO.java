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

	@Bean
	public DataFetcher dataFetcher() {
	    String project = env.getProperty("project", "bento").toLowerCase();
	    switch (project) {
			case "icdc":
				return new IcdcEsFilter();
			case "bento":
				return new BentoEsFilter();
			default:
				logger.warn("Project \"" + project + "\" is not supported! Use default BentoESFilter class");
				return new BentoEsFilter();
		}
	}


	//API Version
	@Value("${bento.api.version}")
	private String bentoApiVersion;

	//Neo4j
	@Value("${neo4j.url}")
	private String neo4jUrl;
	@Value("${neo4j.user}")
	private String neo4jUser;
	@Value("${neo4j.password}")
	private String neo4jPassword;

	//GraphQL
	@Value("${graphql.schema}")
	private String schemaFile;

	public String getEsSchemaFile() {
		return esSchemaFile;
	}

	public void setEsSchemaFile(String esSchemaFile) {
		this.esSchemaFile = esSchemaFile;
	}

	@Value("${graphql.es_schema}")
	private String esSchemaFile;

	//Query Restrictions
	@Value("${allow_graphql_query}")
	private boolean allowGraphQLQuery;
	@Value("${allow_graphql_mutation}")
	private boolean allowGraphQLMutation;

	//Redis Caching
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

	@Value("${es.host}")
	private String esHost;
	@Value("${es.port}")
	private int esPort;
	@Value("${es.scheme}")
	private String esScheme;

	public boolean getEsSignRequests() {
		return esSignRequests;
	}

	public void setEsSignRequests(boolean esSignRequests) {
		this.esSignRequests = esSignRequests;
	}

	@Value("${es.sign.requests:true}")
	private boolean esSignRequests;


	public int getEsPort() {
		return esPort;
	}

	public void setEsPort(int esPort) {
		this.esPort = esPort;
	}

	public String getEsScheme() {
		return esScheme;
	}

	public void setEsScheme(String esScheme) {
		this.esScheme = esScheme;
	}

	@Value(("${es.filter.enabled}"))
	private boolean esFilterEnabled;


	public String getEsHost() {
		return esHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	public boolean getEsFilterEnabled() {
		return esFilterEnabled;
	}

	public void setEsFilterEnabled(boolean esFilterEnabled) {
		this.esFilterEnabled = esFilterEnabled;
	}

	//Testing
	@Value("${test.queries_file}")
	private String testQueriesFile;

	//Getters and Setters
	public String getNeo4jUrl() {
		return neo4jUrl;
	}

	public String getNeo4jUser() {
		return neo4jUser;
	}

	public String getNeo4jPassword() {
		return neo4jPassword;
	}

	public String getSchemaFile() {
		return schemaFile;
	}

	public boolean isAllowGraphQLQuery() {
		return allowGraphQLQuery;
	}

	public boolean isAllowGraphQLMutation() {
		return allowGraphQLMutation;
	}

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

	public String getBentoApiVersion() {
		return bentoApiVersion;
	}

	public String getTestQueriesFile() {
		return testQueriesFile;
	}

	//Setters
	public void setNeo4jUrl(String neo4jUrl) {
		this.neo4jUrl = neo4jUrl;
	}

	public void setNeo4jUser(String neo4jUser) {
		this.neo4jUser = neo4jUser;
	}

	public void setNeo4jPassword(String neo4jPassword) {
		this.neo4jPassword = neo4jPassword;
	}

	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	public void setAllowGraphQLQuery(boolean allowGraphQLQuery) {
		this.allowGraphQLQuery = allowGraphQLQuery;
	}

	public void setAllowGraphQLMutation(boolean allowGraphQLMutation) {
		this.allowGraphQLMutation = allowGraphQLMutation;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public void setRedisTTL(int redisTTL) {
		this.redisTTL = redisTTL;
	}

	public void setRedisUseCluster(Boolean redisUseCluster) {
		this.redisUseCluster = redisUseCluster;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public void setRedisEnabled(Boolean redisEnabled) {
		this.redisEnabled = redisEnabled;
	}

	public void setBentoApiVersion(String bentoApiVersion) {
		this.bentoApiVersion = bentoApiVersion;
	}

	public void setTestQueriesFile(String testQueriesFile) {
		this.testQueriesFile = testQueriesFile;
	}

}
