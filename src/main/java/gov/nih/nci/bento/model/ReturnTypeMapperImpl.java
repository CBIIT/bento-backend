package gov.nih.nci.bento.model;

import gov.nih.nci.bento.constants.Const.ES_FIELDS;
import gov.nih.nci.bento.constants.Const.ES_PARAMS;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReturnTypeMapperImpl {


    // ElasticSearch Default Mapping Value Resolver
    public ReturnTypeMapper getDefault() {
        return (response, returnTypes) -> getMaps(response, returnTypes);
    }
    // ElasticSearch Aggregate Mapping Value Resolver
    @NotNull
    private List<Map<String, Object>> getMaps(SearchResponse response, Map<String, String> returnTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit->{
            Map<String, Object> source = hit.getSourceAsMap();
            Map<String, Object> returnMap = returnTypes.entrySet().stream()
                    .filter(p->source.containsKey(p.getKey()))
                    .collect(Collectors.toMap(k->k.getKey(), v->v.getValue()));
            if (returnMap.size() > 0) result.add(returnMap);
        });
        return result;
    }

    public ReturnTypeMapper getAggregate() {
        return (response, t) -> {
            Aggregations aggregate = response.getAggregations();
            Terms terms = aggregate.get(ES_PARAMS.TERMS_AGGS);
            List<Terms.Bucket> buckets = (List<Terms.Bucket>) terms.getBuckets();
            List<Map<String, Object>> result = new ArrayList<>();
            buckets.forEach(bucket->
                result.add(
                    Map.of(
                            ES_FIELDS.GROUP, bucket.getKey(),
                            ES_FIELDS.COUNT, bucket.getDocCount()
                    )
                )
            );
            return result;
        };
    }

}
