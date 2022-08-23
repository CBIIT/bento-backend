package gov.nih.nci.bento.service.connector;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import gov.nih.nci.bento.model.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

public class AWSClient extends AbstractClient {

    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

    public AWSClient(ConfigurationDAO config) {
        super(config);
    }

    @Override
    public RestHighLevelClient getElasticClient() {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(config.getServiceName());
        signer.setRegionName(config.getRegion());
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(config.getServiceName(), signer, credentialsProvider);

        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(config.getEsHost().trim(), config.getEsPort(), config.getEsScheme())).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }

    @Override
    public RestClient getLowLevelElasticClient() {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(config.getServiceName());
        signer.setRegionName(config.getRegion());
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(config.getServiceName(), signer, credentialsProvider);
        return RestClient.builder(new HttpHost(config.getEsHost().trim(), config.getEsPort(), config.getEsScheme())).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)).build();
    }
}
