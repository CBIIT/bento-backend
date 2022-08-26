package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

public class DefaultClient extends AbstractClient {

    public DefaultClient(ConfigurationDAO config) {
        super(config);
    }

    @Override
    public RestHighLevelClient getElasticClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(config.getEsHost().trim(), config.getEsPort(), config.getEsScheme())));
    }

    @Override
    public RestClient getLowLevelElasticClient() {
        var lowLevelBuilder = RestClient.builder(new HttpHost(config.getEsHost().trim(), config.getEsPort(), config.getEsScheme()));
        return lowLevelBuilder.build();
    }
}
