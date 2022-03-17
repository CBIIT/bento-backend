package gov.nih.nci.bento.search.result;

import gov.nih.nci.bento.classes.QueryResult;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.constants.Const.ES_PARAMS;
import gov.nih.nci.bento.constants.Const.ICDC_FIELDS;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedMin;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TypeMapperImpl {

    // ElasticSearch Default Mapping Value Mapper
    public TypeMapper<List<Map<String, Object>>> getDefault(Set<String> returnTypes) {
        return (response) -> getMaps(response, returnTypes);
    }


    public TypeMapper<QueryResult> getDefaultReturnTypes(Set<String> returnTypes) {
        return (response) -> getDefaultMaps(response, returnTypes);
    }

    @NotNull
    private QueryResult getDefaultMaps(SearchResponse response, Set<String> returnTypes) {
        getListHits(response, returnTypes);
        List<Map<String, Object>> result = getListHits(response, returnTypes);
        return QueryResult.builder()
                .searchHits(result)
                .totalHits(response.getHits().getTotalHits().value)
                .build();
    }

    private List<Map<String, Object>> getListHits(SearchResponse response, Set<String> returnTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit-> {
            Map<String, Object> source = hit.getSourceAsMap();
            Map<String, Object> returnMap = parseReturnMap(returnTypes,source);
            if (returnMap.size() > 0) result.add(returnMap);
        });
        return result;
    }

    // ElasticSearch Aggregate Mapping Value Resolver
    @NotNull
    private List<Map<String, Object>> getMaps(SearchResponse response, Set<String> returnTypes) {
        return getListHits(response, returnTypes);
    }


    @SuppressWarnings("unchecked")
    public TypeMapper<List<Map<String, Object>>> getICDCAggregate() {
        return (response) -> {
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

    public TypeMapper<List<Map<String, Object>>> getAggregate() {
        return (response) -> {
            List<Map<String, Object>> result = new ArrayList<>();
            Aggregations aggregate = response.getAggregations();
            Terms terms = aggregate.get(ES_PARAMS.TERMS_AGGS);
            List<Terms.Bucket> buckets = (List<Terms.Bucket>) terms.getBuckets();
            buckets.forEach(bucket->
                    result.add(
                            Map.of(
                                    BENTO_FIELDS.GROUP,bucket.getKey(),
                                    BENTO_FIELDS.SUBJECTS,bucket.getDocCount()
                            )
                    )
            );
            return result;
        };
    }

    public TypeMapper<List<String>> getStrList(String field) {
        return (response) -> createStrList(response, field);
    }
    // Required Only One Argument
    @NotNull
    private List<String> createStrList(SearchResponse response, String field) {
        List<String> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit-> {
            Map<String, Object> source = hit.getSourceAsMap();
            if (!source.containsKey(field)) throw new IllegalArgumentException();
            result.add((String) source.get(field));
        });
        return result;
    }

    public TypeMapper<Long> getIntTotal() {
        return (response) -> {
            TotalHits hits = response.getHits().getTotalHits();
            return hits.value;
        };
    }

    public TypeMapper<Map<String, Object>> getRange() {
        return (response) -> {
            Aggregations aggregate = response.getAggregations();
            Map<String, Aggregation> responseMap = aggregate.getAsMap();
            Map<String, Object> result = new HashMap<>();

            ParsedMax max = (ParsedMax) responseMap.get("max");
            ParsedMin min = (ParsedMin) responseMap.get("min");

            result.put(BENTO_FIELDS.LOWER_BOUND, min.getValue());
            result.put(BENTO_FIELDS.UPPER_BOUND, max.getValue());
            result.put(BENTO_FIELDS.SUBJECTS, response.getHits().getTotalHits().value);
            return result;
        };
    }

    public TypeMapper<QueryResult> getHighLightFragments(String field, HighLightMapper mapper) {
        return (response) -> {
            List<Map<String, Object>> result = new ArrayList<>();
            SearchHit[] hits = response.getHits().getHits();
            Arrays.asList(hits).forEach(hit-> {
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                Map<String, Object> source = hit.getSourceAsMap();
                HighlightField highlightField = highlightFieldMap.get(field);
                Text[] texts = highlightField.getFragments();
                Arrays.stream(texts).forEach(text->{
                    result.add(
                            mapper.getMap(source, text)
                    );
                });
            });
            return QueryResult.builder()
                    .searchHits(result)
                    .totalHits(response.getHits().getTotalHits().value)
                    .build();
        };
    }

    public TypeMapper<QueryResult> getMapWithHighlightedFields(Set<String> returnTypes) {
        return (response) -> {
            List<Map<String, Object>> result = new ArrayList<>();
            SearchHit[] hits = response.getHits().getHits();
            Arrays.asList(hits).forEach(hit-> {
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                Map<String, Object> source = hit.getSourceAsMap();

                Map<String, Object> returnMap = parseReturnMap(returnTypes, source);
                highlightFieldMap.forEach((k,highlightField)->{
                    Text[] texts = highlightField.getFragments();
                    Optional<String> text = Arrays.stream(texts).findFirst().map(v->v.toString()).stream().findFirst();
                    // Set Highlight Field & Get First Found Match Keyword
                    text.ifPresent(v->returnMap.put(BENTO_FIELDS.HIGHLIGHT, v));
                });
                if (returnMap.size() > 0) result.add(returnMap);
            });
        return QueryResult.builder()
                .searchHits(result)
                .totalHits(response.getHits().getTotalHits().value)
                .build();
        };
    }

    private Map<String, Object> parseReturnMap(Set<String> returnTypes, Map<String, Object> source) {
        return returnTypes.stream()
                .filter(p->source.containsKey(p))
                .collect(HashMap::new, (k,v)->k.put(v, source.get(v)), HashMap::putAll);
    }

    @SuppressWarnings("unchecked")
    public TypeMapper<List<Map<String, Object>>> getArmProgram() {

        return (response) -> {
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
                                    BENTO_FIELDS.ARM,subBucket.getKey(),
                                    BENTO_FIELDS.CASE_SIZE,subBucket.getDocCount(),
                                    BENTO_FIELDS.SIZE,subBucket.getDocCount()
                            ));
                        });
                        result.add(
                                Map.of(
                                        BENTO_FIELDS.PROGRAM, bucket.getKey(),
                                        BENTO_FIELDS.CASE_SIZE,bucket.getDocCount(),
                                        BENTO_FIELDS.CHILDREN, studies
                                )
                        );

                    }
            );
            return result;
        };
    }
}
