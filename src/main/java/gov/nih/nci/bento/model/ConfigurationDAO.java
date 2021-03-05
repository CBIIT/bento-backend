package gov.nih.nci.bento.model;
import gov.nih.nci.bento.BentoApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.annotation.PostConstruct;

/**
 * A Configuration bean read configuration setting from
 * classpth:application.properties. This class provides configuration settings.
 */

@Configuration
@PropertySource("classpath:application.properties")
public class ConfigurationDAO {

	private static final Logger logger = LogManager.getLogger(ConfigurationDAO.class);

	@Value("${neo4j.graphql.endpoint.schema_endpoint}")
	private String neo4jGraphQLSchemaEndpoint;

	@Value("${graphql.schema}")
	private String schemaFile;

	@Value("${allow_graphql_query}")
	private boolean allowGraphQLQuery;

	@Value("${allow_graphql_mutation}")
	private boolean allowGraphQLMutation;

	@Value("${enable.redis}")
	private Boolean redisEnabled;

	@Value("${redis.use_cluster}")
	private Boolean redisUseCluster;

	@Value("${redis.port}")
	private int redisPort;

	@Value("${redis.ttl}")
	private int redisTTL;

	@Value("${bento.api.version}")
	private String bentoApiVersion;

	//Variables from environment
	private String redisHost;
	private final String redisHostEnvKey = "REDIS_HOST";

	private String neo4jHttpHeaderAuthorization;
	private final String neo4jHttpHeaderAuthorizationEnvKey = "NEO4J_AUTHORIZATION";

	private String neo4jGraphQLEndPoint;
	private final String neo4jGraphQLEndPointEnvKey = "NEO4J_GRAPHQL_ENDPOINT";

	@PostConstruct
	public void readSystemVariables(){
		neo4jGraphQLEndPoint = System.getenv(neo4jGraphQLEndPointEnvKey);
		neo4jHttpHeaderAuthorization = System.getenv(neo4jHttpHeaderAuthorizationEnvKey);
		if (neo4jGraphQLEndPoint == null || neo4jHttpHeaderAuthorization == null){
			logger.error("An essential environment variable was unable to be read, please verify both" +
					String.format(
							" %s and %s are properly set",
							neo4jGraphQLEndPointEnvKey,
							neo4jHttpHeaderAuthorizationEnvKey
					)
			);
			System.exit(1);
		}
		if (redisEnabled){
			redisHost = System.getenv(redisHostEnvKey);
			if (redisHost == null){
				logger.warn(
						String.format(
								"The %s environment variable could not be read, " +
								"Redis caching functionality will be disabled",
								redisHostEnvKey)
				);
				redisEnabled = false;
			}
		}

	}

	
	public String getNeo4jGraphQLEndPoint() {
		return neo4jGraphQLEndPoint;
	}

	public void setNeo4jGraphQLEndPoint(String neo4jGraphQLEndPoint) {
		this.neo4jGraphQLEndPoint = neo4jGraphQLEndPoint;
	}

	public String getNeo4jGraphQLSchemaEndpoint() {
		return neo4jGraphQLSchemaEndpoint;
	}

	public void setNeo4jGraphQLSchemaEndpoint(String neo4jGraphQLSchemaEndpoint) {
		this.neo4jGraphQLSchemaEndpoint = neo4jGraphQLSchemaEndpoint;
	}

	public String getNeo4jHttpHeaderAuthorization() {
		return neo4jHttpHeaderAuthorization;
	}

	public void setNeo4jHttpHeaderAuthorization(String neo4jHttpHeaderAuthorization) {
		this.neo4jHttpHeaderAuthorization = neo4jHttpHeaderAuthorization;
	}

	public String getSchemaFile() {
		return schemaFile;
	}

	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	public boolean isAllowGraphQLQuery() {
		return allowGraphQLQuery;
	}

	public void setAllowGraphQLQuery(boolean allowGraphQLQuery) {
		this.allowGraphQLQuery = allowGraphQLQuery;
	}

	public boolean isAllowGraphQLMutation() {
		return allowGraphQLMutation;
	}

	public void setAllowGraphQLMutation(boolean allowGraphQLMutation) {
		this.allowGraphQLMutation = allowGraphQLMutation;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public int getRedisTTL() {
		return redisTTL;
	}

	public void setRedisTTL(int redisTTL) {
		this.redisTTL = redisTTL;
	}

	public Boolean getRedisUseCluster() {
		return redisUseCluster;
	}

	public void setRedisUseCluster(Boolean redisUseCluster) {
		this.redisUseCluster = redisUseCluster;
	}

	public int getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public Boolean getRedisEnabled() {
		return redisEnabled;
	}

	public void setRedisEnabled(Boolean redisEnabled) {
		this.redisEnabled = redisEnabled;
	}

	public String getBentoApiVersion(){ return bentoApiVersion; }

	public void setBentoApiVersion(String bentoApiVersion){ this.bentoApiVersion = bentoApiVersion; }
}
