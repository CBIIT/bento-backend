package gov.nih.nci.bento.config;

import gov.nih.nci.bento.model.BentoEsSearch;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.model.ICDCEsSearch;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.service.ESServiceImpl;
import gov.nih.nci.bento.service.EsSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@RequiredArgsConstructor
@Getter
public class ConfigurationDAO {

	private static final Logger logger = LogManager.getLogger(ConfigurationDAO.class);
	private final Environment env;
	private final TypeMapperImpl typeMapper;
	private final EsSearch esSearch = new ESServiceImpl(this);

	@Bean
	public DataFetcher dataFetcher() {
	    String project = env.getProperty("project", "bento").toLowerCase();
	    switch (project) {
			case "icdc":
				return new ICDCEsSearch(esSearch, typeMapper);
			case "bento":
				return new BentoEsSearch(esSearch, typeMapper);
			default:
				logger.warn("Project \"" + project + "\" is not supported! Use default BentoESFilter class");
				return new BentoEsSearch(esSearch, typeMapper);
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

	@Value("${es.sign.requests:true}")
	private boolean esSignRequests;

	@Value(("${es.filter.enabled}"))
	private boolean esFilterEnabled;

	//Testing
	@Value("${test.queries_file}")
	private String testQueriesFile;

}
