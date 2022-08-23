package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.model.ConfigurationDAO;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.RestClient;

@RequiredArgsConstructor
public abstract class AbstractClient {

    protected final ConfigurationDAO config;

    public abstract RestHighLevelClient getElasticClient();
    public abstract RestClient getLowLevelElasticClient();
}
