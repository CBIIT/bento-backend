package gov.nih.nci.bento.service.connector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class DefaultClient extends AbstractClient {

    public DefaultClient(ConfigurationDAO config) {
        super(config);
    }

    @Override
    public RestClient getRestClient() {
        var lowLevelBuilder = RestClient.builder(new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme()));
        return lowLevelBuilder.build();
    }

    @Override
    public ElasticsearchClient getElasticClient() {

        RestClient client = RestClient.builder(new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme())).build();
        ElasticsearchTransport transport = new RestClientTransport(
                client, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
