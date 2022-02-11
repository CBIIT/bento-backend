package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.model.ConfigurationDAO;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.RestClient;

@RequiredArgsConstructor
public abstract class AbstractClient {

    protected final ConfigurationDAO config;

    public RestClient getRestConnector() {
        if (config.isEsSignRequests()) new AWSClient(config).getRestClient();
        return getRestClient();
    }

    public abstract RestClient getRestClient();
}
