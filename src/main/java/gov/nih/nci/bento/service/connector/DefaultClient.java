package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

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
    public org.opensearch.client.RestClient getLowLevelElasticClient() {
        var lowLevelBuilder = org.opensearch.client.RestClient.builder(new HttpHost(config.getEsHost().trim(), config.getEsPort(), config.getEsScheme()));
        return lowLevelBuilder.build();
    }
}
