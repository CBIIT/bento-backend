package gov.nih.nci.bento.model;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReturnTypeMapperImpl {


    // ElasticSearch Default Mapping Value Resolver
    public ReturnTypeMapper getDefault() {
        ReturnTypeMapper resolver = (response, returnTypes) -> getMaps(response, returnTypes);
        return resolver;
    }
    // ElasticSearch Aggregate Mapping Value Resolver
    @NotNull
    private List<Map<String, Object>> getMaps(SearchResponse response, Map<String, String> returnTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit->{

            Map<String, Object> source = hit.getSourceAsMap();
            Map<String, Object> returnMap = new HashMap<>();
            returnTypes.forEach((k,v)->{
                if (source.containsKey(k)) returnMap.put(k, source.get(v));
            });
            if (returnMap.size() > 0) result.add(returnMap);
        });
        return result;
    }
//
//    public TypeResolver getAggregate() {
//        TypeResolver resolver = (response, t) -> {
//            Aggregate aggregate = response.aggregations().get(ES_PARAMS.TERMS_AGGS);
//            TermsAggregateBase base = (TermsAggregateBase) aggregate._get();
//
//            Buckets tBuckets= base.buckets();
//            List<StringTermsBucket> arrays= tBuckets.array();
//
//            List<Map<String, Object>> result = new ArrayList<>();
//            arrays.forEach(bucket->{
//                Map<String,Object> map = Map.of(
//                        "group", bucket.key(),
//                        "count", bucket.docCount()
//                );
//                result.add(map);
//            });
//            return result;
//        };
//        return resolver;
//    }

}
