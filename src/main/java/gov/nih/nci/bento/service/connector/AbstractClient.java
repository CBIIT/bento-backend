package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.config.ConfigurationDAO;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.RestHighLevelClient;

@RequiredArgsConstructor
public abstract class AbstractClient {

    protected final ConfigurationDAO config;

    public RestHighLevelClient getElasticRestClient() {
        if (config.isEsSignRequests()) new AWSClient(config).getElasticClient();
        return getElasticClient();
    }

    public abstract RestHighLevelClient getElasticClient();
}
