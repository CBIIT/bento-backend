package gov.nih.nci.bento.search.result;

import gov.nih.nci.bento.classes.QueryResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TypeMapperService {

    TypeMapper<List<Map<String, Object>>> getDefault(Set<String> returnTypes);
    TypeMapper<QueryResult> getDefaultReturnTypes(Set<String> returnTypes);
    TypeMapper<List<Map<String, Object>>> getAggregate();
    TypeMapper<Integer> getAggregateTotalCnt();
    TypeMapper<List<String>> getStrList(String field);
    TypeMapper<Long> getIntTotal();
    TypeMapper<Map<String, Object>> getRange();
    TypeMapper<QueryResult> getHighLightFragments(String field, HighLightMapper mapper);
    TypeMapper<QueryResult> getMapWithHighlightedFields(Set<String> returnTypes);
    TypeMapper<List<Map<String, Object>>> getArmProgram();

}