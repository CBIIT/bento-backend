package gov.nih.nci.bento.service.connector;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import gov.nih.nci.bento.config.ConfigurationDAO;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

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
                        new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme())).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }
}
