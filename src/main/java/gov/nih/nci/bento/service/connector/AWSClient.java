package gov.nih.nci.bento.service.connector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;

public class AWSClient extends AbstractClient {

    private final String serviceName ="es";
    private final String region = "us-east-1";
    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

    public AWSClient(ConfigurationDAO config) {
        super(config);
    }

    @Override
    public RestClient getRestClient() {

        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return RestClient.builder(new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme())).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)).build();
    }

    @Override
    public ElasticsearchClient getElasticClient() {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);

        RestClient client = RestClient.builder(new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme())).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)).build();
        ElasticsearchTransport transport = new RestClientTransport(
                client, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);

    }
}
