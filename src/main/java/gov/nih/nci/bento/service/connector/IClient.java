package gov.nih.nci.bento.service.connector;

import org.opensearch.client.RestClient;

interface IClient {
    RestClient getRestClient();
}
