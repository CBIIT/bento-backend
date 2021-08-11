package gov.nih.nci.bento.service;

import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Service
public class ESFilterDataFetcher {
    private static final Logger logger = LogManager.getLogger(ESFilterDataFetcher.class);

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

    private List<String> searchSubjects3(Map<String, Object> params) throws IOException {
        final String ID_FIELD = "subject_id";
        final String DASHBOARD_INDEX = "dashboard";

        String[] includeFields = new String[] {ID_FIELD};

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_study")
                .field("studies");
        sourceBuilder.aggregation(aggregation);

        sourceBuilder.query(buildQuery(params));
        sourceBuilder.fetchSource(includeFields, null);
        sourceBuilder.sort(new FieldSortBuilder(ID_FIELD).order(SortOrder.ASC));
        sourceBuilder.size(10000);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        SearchRequest searchRequest = new SearchRequest(DASHBOARD_INDEX);
        final Scroll scroll = new Scroll(TimeValue.timeValueSeconds(10L));
        searchRequest.scroll(scroll);
        searchRequest.source(sourceBuilder);

        SearchResponse response = esService.search(searchRequest);
        List<String> subject_ids = esService.collectAll(response, scroll, ID_FIELD);

        return subject_ids;
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
}
