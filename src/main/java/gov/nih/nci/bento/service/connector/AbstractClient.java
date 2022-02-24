package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.model.ConfigurationDAO;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

@RequiredArgsConstructor
public abstract class AbstractClient {

    protected final ConfigurationDAO config;

    public RestClient getRestConnector() {
        if (config.isEsSignRequests()) new AWSClient(config).getRestClient();
        return getRestClient();
    }

    public RestHighLevelClient getElasticRestClient() {
        if (config.isEsSignRequests()) new AWSClient(config).getElasticClient();
        return getElasticClient();
    }


    public abstract RestClient getRestClient();
    public abstract RestHighLevelClient getElasticClient();
}
