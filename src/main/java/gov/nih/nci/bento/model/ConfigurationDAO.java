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

	@Value("${allow_graphql_query}")
	private boolean allowGraphQLQuery;

	@Value("${allow_graphql_mutation}")
	private boolean allowGraphQLMutation;

	@Value("${cache.enabled}")
	private boolean enableCache;

	@Value("${cache.time_to_live_min}")
	private double cacheTimeToLiveMin;

	@Value("${cache.max_size_mb}")
	private double cacheMaxSizeMB;

	@Value("${cache.sweep_frequency_min}")
	private double cacheSweepFrequencyMin;
	
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

	public boolean isEnableCache() {
		return enableCache;
	}

	public void setEnableCache(boolean enableCache) {
		this.enableCache = enableCache;
	}

	public double getCacheTimeToLiveMin() {
		return cacheTimeToLiveMin;
	}

	public void setCacheTimeToLiveMin(double cacheTimeToLive) {
		this.cacheTimeToLiveMin = cacheTimeToLive;
	}

	public double getCacheMaxSizeMB() {
		return cacheMaxSizeMB;
	}

	public void setCacheMaxSizeMB(int cacheMaxSizeMB) {
		this.cacheMaxSizeMB = cacheMaxSizeMB;
	}

	public double getCacheSweepFrequencyMin() {
		return cacheSweepFrequencyMin;
	}

	public void setCacheSweepFrequencyMin(double cacheSweepFrequencyMin) {
		this.cacheSweepFrequencyMin = cacheSweepFrequencyMin;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
