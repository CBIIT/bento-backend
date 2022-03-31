package gov.nih.nci.bento.service;

import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.config.ConfigurationDAO;
import gov.nih.nci.bento.search.result.TypeMapper;
import gov.nih.nci.bento.service.connector.DefaultClient;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("ESService")
@RequiredArgsConstructor
public class ESServiceImpl implements EsSearch {
    private static final Logger logger = LogManager.getLogger(RedisService.class);
    private final ConfigurationDAO config;

    @Override
    public <T> T elasticSend(SearchRequest request, TypeMapper<T> mapper) throws IOException {
        SearchResponse searchResponse = null;
        try (RestHighLevelClient elasticClient = new DefaultClient(config).getElasticClient()) {
            searchResponse = elasticClient.search(request, RequestOptions.DEFAULT);
        } catch (RuntimeException e) {
            logger.error(e.toString());
        }
        T result = mapper.get(searchResponse);
        return result;
    }

    @Override
    public Map<String, Object> elasticMultiSend(@NotNull List<MultipleRequests> requests) throws IOException {
        MultiSearchRequest multiRequests = new MultiSearchRequest();
        requests.forEach(r->multiRequests.add(r.getRequest()));
        Map<String, Object> result = new HashMap<>();
        try (RestHighLevelClient elasticClient = new DefaultClient(config).getElasticClient()) {
            MultiSearchResponse response = elasticClient.msearch(multiRequests, RequestOptions.DEFAULT);
            MultiSearchResponse.Item[] responseResponses = response.getResponses();
            result = getMultiResponse(responseResponses, requests);
        }
        catch (RuntimeException e) {
            logger.error(e.toString());
        }
        return result;
    }

    private Map<String, Object> getMultiResponse(MultiSearchResponse.Item[] response, List<MultipleRequests> requests) {
        Map<String, Object> result = new HashMap<>();
        final int[] index = {0};
        List.of(response).forEach(item->{
            MultipleRequests data = requests.get(index[0]);
            result.put(data.getName(),data.getTypeMapper().get(item.getResponse()));
            index[0] += 1;
        });
        return result;
    }

    @Override
    public QueryParam CreateQueryParam(DataFetchingEnvironment env) {
        return QueryParam.builder()
                .args(env.getArguments())
                .outputType(env.getFieldType())
                .build();
    }
}