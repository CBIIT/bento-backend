package gov.nih.nci.bento.model;

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


    // ElasticSearch Default Mapping Value Resolver
    public TypeMapper<List<Map<String, Object>>> getDefault() {
        return (response, returnTypes) -> getMaps(response, returnTypes);
    }

    public TypeMapper<QueryResult> getDefaultReturnTypes(Map<String, String> returnTypes) {
        return (response, r) -> getMaps_Test(response, returnTypes);
    }

    // ElasticSearch Aggregate Mapping Value Resolver
    @NotNull
    // TODO
    private QueryResult getMaps_Test(SearchResponse response, Map<String, String> returnTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit-> {
            Map<String, Object> source = hit.getSourceAsMap();
            Map<String, Object> returnMap = parseReturnMap(returnTypes,source);
            if (returnMap.size() > 0) result.add(returnMap);
        });
        return QueryResult.builder()
                .searchHits(result)
                .totalHits(response.getHits().getTotalHits().value)
                .build();
    }

    // ElasticSearch Aggregate Mapping Value Resolver
    // TODO TOBE REMOVED
    @NotNull
    private List<Map<String, Object>> getMaps(SearchResponse response, Map<String, String> returnTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit-> {
            Map<String, Object> source = hit.getSourceAsMap();
            Map<String, Object> returnMap = parseReturnMap(returnTypes,source);
            if (returnMap.size() > 0) result.add(returnMap);
        });
        return result;
    }


    public TypeMapper getICDCAggregate() {
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

    public TypeMapper<List<Map<String, Object>>> getAggregate() {
        return (response, t) -> {
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
        return (response, returnTypes) -> createStrList(response, field);
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
        return (response, t) -> {
            TotalHits hits = response.getHits().getTotalHits();
            return hits.value;
        };
    }

    public TypeMapper<Map<String, Object>> getRange() {
        return (response, t) -> {
            Aggregations aggregate = response.getAggregations();
            Map<String, Aggregation> responseMap = aggregate.getAsMap();
            Map<String, Object> result = new HashMap<>();

            ParsedMax max = (ParsedMax) responseMap.get("max");
            ParsedMin min = (ParsedMin) responseMap.get("min");

            result.put(BENTO_FIELDS.LOWER_BOUND, min.getValue());
            result.put(BENTO_FIELDS.UPPER_BOUND, max.getValue());
            // TODO
            result.put(BENTO_FIELDS.SUBJECTS, 100);
            return result;
        };
    }

    public TypeMapper<List<Map<String, Object>>> getAboutPage() {
        return (response, t) -> {
            List<Map<String, Object>> result = new ArrayList<>();
            SearchHit[] hits = response.getHits().getHits();
            Arrays.asList(hits).forEach(hit-> {
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                Map<String, Object> source = hit.getSourceAsMap();
                HighlightField field = highlightFieldMap.get(BENTO_FIELDS.CONTENT_PARAGRAPH);
                Text[] texts = field.getFragments();
                Arrays.stream(texts).forEach(text->{
                    result.add(
                            Map.of(
                                    BENTO_FIELDS.TYPE, BENTO_FIELDS.ABOUT,
                                    BENTO_FIELDS.PAGE, source.get(BENTO_FIELDS.PAGE),
                                    BENTO_FIELDS.TITLE,source.get(BENTO_FIELDS.TITLE),
                                    BENTO_FIELDS.TEXT, text
                            )
                    );
                });
            });
            return result;
        };
    }

    public TypeMapper<List<Map<String, Object>>> getHighLightFragments(String field, IBentoHighLightMapper mapper) {
        return (response, t) -> {
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
            return result;
        };
    }

    public TypeMapper<QueryResult> getMapWithHighlightedFields(Map<String, String> returnTypes) {
        return (response, t) -> {
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

    private Map<String, Object> parseReturnMap(Map<String, String> returnTypes, Map<String, Object> source) {
        return returnTypes.entrySet().stream()
                .filter(p->source.containsKey(p.getKey()))
                .collect(HashMap::new, (k,v)->k.put(v.getKey(), source.get(v.getKey())), HashMap::putAll);
    }

    public TypeMapper<List<Map<String, Object>>> getArmProgram() {

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
