package gov.nih.nci.bento.service;

import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Service
public class ESFilterDataFetcher {
    private static final Logger logger = LogManager.getLogger(RedisFilterDataFetcher.class);

    @Autowired ESService esService;

    public RuntimeWiring buildRuntimeWiring(){
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchSubjects3", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects3(args);
                        })
                )
                .build();
    }

    private List<String> searchSubjects3(Map<String, Object> params) throws FilterException {
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(buildQuery(params));
            //Todo: limited to 10,000 IDs by ES, need to use "search after" to get all subject IDs
            sourceBuilder.from(0);
            sourceBuilder.size(10000);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices("dashboard");
            searchRequest.source(sourceBuilder);

            SearchResponse response = esService.query(searchRequest);
            List<String> subject_ids = new ArrayList<>();
            for (var hit: response.getHits()) {
                subject_ids.add((String)hit.getSourceAsMap().get("subject_id"));
            }

            return subject_ids;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    private QueryBuilder buildQuery(Map<String, Object> params) {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        for (var key: params.keySet()) {
            List<String> valueSet = (List<String>)params.get(key);
            if (valueSet.size() > 0) {
                queryBuilder.filter(QueryBuilders.termsQuery(key, valueSet));
            }
        }

        return queryBuilder;
    }

    private class FilterException extends Exception {
        private String message = "";

        FilterException(ArrayList<String> invalidGroups, ArrayList<String> invalidParams){
            if (!invalidGroups.isEmpty()){
                message = String.format("The following filter groups were not found by the initialization queries: %s",
                        String.join(", ",invalidGroups));
            }
            if (!invalidParams.isEmpty()){
                if (!message.isEmpty()){
                    message += "; ";
                }
                message += String.format("The following parameters are not mapped to a filter group: %s",
                        String.join(", ",invalidParams));
            }
        }

        @Override
        public String getMessage(){
            return message;
        }
    }
}
