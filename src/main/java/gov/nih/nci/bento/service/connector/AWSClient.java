package gov.nih.nci.bento.service.connector;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import gov.nih.nci.bento.model.ConfigurationDAO;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;

@RequiredArgsConstructor
public class AWSClient implements IClient {

    private final String serviceName ="es";
    private final String region = "us-east-1";
    protected final ConfigurationDAO config;
    @Override
    public RestClient getRestClient() {

        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return RestClient.builder(new HttpHost(config.getEsHost(), config.getEsPort(), config.getEsScheme())).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)).build();
    }
}
