package gov.nih.nci.bento.service.connector;

import gov.nih.nci.bento.config.ConfigurationDAO;
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
                        new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme())));
    }
}
