package gov.nih.nci.bento.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import graphql.schema.idl.RuntimeWiring;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Service
public class ESFilterDataFetcher {
    private static final Logger logger = LogManager.getLogger(ESFilterDataFetcher.class);

    @Autowired ESService esService;

    private Gson gson = new GsonBuilder().serializeNulls().create();

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
        final String DASHBOARD_END_POINT = "dashboard/_search";
        Request request = new Request("GET", DASHBOARD_END_POINT);
        Map<String, Object> query = buildQuery(params);
        query.put("size", 10);
        request.setJsonEntity(gson.toJson(query));

        List<String> subject_ids = esService.collectAll(request, ID_FIELD);

        return subject_ids;
    }

    private Map<String, Object> buildQuery(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> query = new HashMap<String, Object>();
        result.put("query", query);
        Map<String, Object> bool = new HashMap<>();
        query.put("bool", bool);
        List<Object> filter = new ArrayList<>();
        bool.put("filter", filter);

        for (var key: params.keySet()) {
            List<String> valueSet = (List<String>)params.get(key);
            if (valueSet.size() > 0) {
                Map<String, Object> terms = new HashMap<>();
                filter.add(terms);
                Map<String, List<String>> field = new HashMap<>();
                terms.put("terms", field);
                field.put(key, valueSet);
            }
        }

        return result;
    }
}
