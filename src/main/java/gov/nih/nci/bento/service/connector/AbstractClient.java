package gov.nih.nci.bento.service.connector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import gov.nih.nci.bento.model.ConfigurationDAO;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestClient;

@RequiredArgsConstructor
public abstract class AbstractClient {

    protected final ConfigurationDAO config;

    public RestClient getRestConnector() {
        if (config.isEsSignRequests()) new AWSClient(config).getRestClient();
        return getRestClient();
    }

    public ElasticsearchClient getElasticRestClient() {
        if (config.isEsSignRequests()) new AWSClient(config).getElasticClient();
        return getElasticClient();
    }


    public abstract RestClient getRestClient();
    public abstract ElasticsearchClient getElasticClient();
}
