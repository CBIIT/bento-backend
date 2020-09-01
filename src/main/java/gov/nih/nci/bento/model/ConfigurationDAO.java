package gov.nih.nci.bento.model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * A Configuration bean read configuration setting from
 * classpth:application.properties. This class provides configuration settings.
 */

@Configuration
@PropertySource("classpath:application.properties")
public class ConfigurationDAO {

	
	@Value("${neo4j.authorization}")
	private String neo4jHttpHeaderAuthorization;

	@Value("${neo4j.graphql.endpoint}")
	private String neo4jGraphQLEndPoint;

	@Value("${neo4j.graphql.endpoint.schema_endpoint}")
	private String neo4jGraphQLSchemaEndpoint;

	@Value("${graphql.schema}")
	private String schemaFile;

	@Value("${allow_grapqh_query}")
	private boolean allowGraphQLQuery;
	
	
	@Value("${allow_graphql_mutation}")
	private boolean allowGraphQLMutation;

	@Value("${redis.host}")
	private String redisHost;
	@Value("${redis.password}")
	private String redisPassword;
	@Value("${redis.ttl}")
	private int redisTTL;

	
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

	public String getRedisPassword() {
		return redisPassword;
	}

	public void setRedisPassword(String redisPassword) {
		this.redisPassword = redisPassword;
	}

	public int getRedisTTL() {
		return redisTTL;
	}

	public void setRedisTTL(int redisTTL) {
		this.redisTTL = redisTTL;
	}
}
