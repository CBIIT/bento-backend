package gov.nih.nci.bento.model;

import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.ES_PARAMS;
import gov.nih.nci.bento.constants.Const.ICDC_FIELDS;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TypeMapper {


    // ElasticSearch Default Mapping Value Resolver
    public ITypeMapper getDefault() {
        return (response, returnTypes) -> getMaps(response, returnTypes);
    }
    // ElasticSearch Aggregate Mapping Value Resolver
    @NotNull
    private List<Map<String, Object>> getMaps(SearchResponse response, Map<String, String> returnTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit-> {
                    Map<String, Object> source = hit.getSourceAsMap();
                    Map<String, Object> returnMap = new HashMap<>();
                    returnTypes.forEach((k, v) -> {
                        if (source.containsKey(k)) returnMap.put(k, source.get(v));
                    });
                    if (returnMap.size() > 0) result.add(returnMap);
        });
// TODO
//                Map<String, Object> returnMap = returnTypes.entrySet().stream()
//                        .filter(p->source.containsKey(p.getKey()))
//                        .collect(Collectors.toMap(k->k.getKey(), v->source.get(v.getKey())));
//                if (returnMap.size() > 0) result.add(returnMap);
//            return result;
        return result;
    }


    public ITypeMapper getICDCAggregate() {
        return (response, t) -> {
            Aggregations aggregate = response.getAggregations();
            Terms terms = aggregate.get(ES_PARAMS.TERMS_AGGS);
            List<Terms.Bucket> buckets = (List<Terms.Bucket>) terms.getBuckets();
            List<Map<String, Object>> result = new ArrayList<>();
            buckets.forEach(bucket->
                    result.add(
                            Map.of(
                                    ICDC_FIELDS.GROUP, bucket.getKey(),
                                    ICDC_FIELDS.COUNT, bucket.getDocCount()
                            )
                    )
            );
            return result;
        };
    }

    public ITypeMapper getAggregate() {
        return (response, t) -> {
            List<Map<String, Object>> result = new ArrayList<>();
            try {
                Aggregations aggregate = response.getAggregations();
                Terms terms = aggregate.get(ES_PARAMS.TERMS_AGGS);
                List<Terms.Bucket> buckets = (List<Terms.Bucket>) terms.getBuckets();

                buckets.forEach(bucket->
                        result.add(
                                Map.of(
                                        BENTO_FIELDS.GROUP, bucket.getKey(),
                                        BENTO_FIELDS.SUBJECTS, (int) bucket.getDocCount()
                                )
                        )
                );
            }catch (Exception e) {

                System.out.println(e.toString());
            }


            return result;
        };
    }

    public ITypeMapper getIntTotal() {
        return (response, t) -> {
            TotalHits hits = response.getHits().getTotalHits();
            return (int) hits.value;
        };
    }

    public ITypeMapper getRange() {

        // TODO
        return (response, t) -> {
            Aggregations aggregate = response.getAggregations();
            Map<String, Aggregation> responseMap = aggregate.getAsMap();

            Map<String, Object> result = new HashMap<>();
            result.put(BENTO_FIELDS.LOWER_BOUND, responseMap.containsKey("max") ? 100 : null);
            result.put(BENTO_FIELDS.UPPER_BOUND, responseMap.containsKey("min") ? 200 : null);

            result.put(BENTO_FIELDS.SUBJECTS, 100);


//            Map<String, Object> result = Map.of(
//                    ES_FIELDS.LOWER_BOUND, responseMap.containsKey("max") ? responseMap.get("max") : null,
//                    ES_FIELDS.UPPER_BOUND, responseMap.containsKey("min") ? responseMap.get("min") : null,
//                    ES_FIELDS.SUBJECTS, 100
//            );
            return result;
        };
    }

    public ITypeMapper getArmProgram() {

        return (response, t) -> {
            Aggregations aggregate = response.getAggregations();
            Terms terms = aggregate.get(ES_PARAMS.TERMS_AGGS);
            List<Terms.Bucket> buckets = (List<Terms.Bucket>) terms.getBuckets();
            List<Map<String, Object>> result = new ArrayList<>();
            buckets.forEach(bucket-> {
                        Aggregations subAggregate = bucket.getAggregations();
                        Terms subTerms = subAggregate.get(ES_PARAMS.TERMS_AGGS);
                        List<Terms.Bucket> subBuckets = (List<Terms.Bucket>) subTerms.getBuckets();
                        List<Map<String, Object>> studies = new ArrayList<>();
                        subBuckets.forEach((subBucket)->{
                            studies.add(Map.of(
                                    BENTO_FIELDS.ARM, subBucket.getKey(),
                                    BENTO_FIELDS.CASE_SIZE, (int) subBucket.getDocCount(),
                                    BENTO_FIELDS.SIZE, (int) subBucket.getDocCount()
                            ));
                        });
                        result.add(
                                Map.of(
                                        BENTO_FIELDS.PROGRAM, bucket.getKey(),
                                        BENTO_FIELDS.CASE_SIZE, (int) bucket.getDocCount(),
                                        BENTO_FIELDS.CHILDREN, studies
                                )
                        );

                    }
            );
            return result;
        };
    }
}
