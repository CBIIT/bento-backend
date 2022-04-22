package gov.nih.nci.bento.search.result;

import gov.nih.nci.bento.classes.QueryResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TypeMapperService {

    TypeMapper<List<Map<String, Object>>> getList(Set<String> returnTypes);
    TypeMapper<QueryResult> getQueryResult(Set<String> returnTypes);
    TypeMapper<List<Map<String, Object>>> getAggregate();
    // TODO ICDC PROJECT TO BE DELETED
    TypeMapper<List<Map<String, Object>>> getICDCAggregate();
    TypeMapper<Integer> getAggregateTotalCnt();
    TypeMapper<Integer> getNestedAggregateTotalCnt();
    TypeMapper<List<String>> getStrList(String field);
    TypeMapper<Long> getIntTotal();
    TypeMapper<Map<String, Object>> getRange();
    TypeMapper<QueryResult> getHighLightFragments(String field, HighLightMapper mapper);
    TypeMapper<QueryResult> getMapWithHighlightedFields(Set<String> returnTypes);
    TypeMapper<List<Map<String, Object>>> getArmProgram();
    TypeMapper<List<Map<String, Object>>> getICDCArmProgram();
    TypeMapper<QueryResult> getNestedAggregate();
    TypeMapper<List<Map<String, Object>>> getNestedAggregateList();
    // TODO TO BE DELETED
    TypeMapper<QueryResult> getICDCNestedAggregate();
    // TODO TO BE DELETED
    TypeMapper<List<Map<String, Object>>> getICDCNestedAggregateList();
    TypeMapper<Integer> getIntTotalNestedAggregate();
    TypeMapper<Float> getSumAggregate();
    TypeMapper<Float> getNestedSumAggregate();
}