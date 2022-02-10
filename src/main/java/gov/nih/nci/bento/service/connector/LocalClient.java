package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.model.ConfigurationDAO;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;

@RequiredArgsConstructor
public class LocalClient implements IClient {
    protected final ConfigurationDAO config;

    @Override
    public RestClient getRestClient() {
        var lowLevelBuilder = RestClient.builder(new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme()));
        return lowLevelBuilder.build();
    }
}
