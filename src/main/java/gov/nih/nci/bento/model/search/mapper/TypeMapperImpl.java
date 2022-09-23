package gov.nih.nci.bento.model.search.mapper;

import gov.nih.nci.bento.model.search.query.QueryResult;
import org.jetbrains.annotations.NotNull;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TypeMapperImpl implements TypeMapperService {

    public TypeMapper<List<Map<String, Object>>> getList(Set<String> returnTypes) {
        return (response) -> getMaps(response, returnTypes);
    }

    @NotNull
    private List<Map<String, Object>> getMaps(SearchResponse response, Set<String> returnTypes) {
        return getListHits(response, returnTypes);
    }

    public TypeMapper<QueryResult> getQueryResult(Set<String> returnTypes) {
        return (response) -> getDefaultMaps(response, returnTypes);
    }

    @NotNull
    private QueryResult getDefaultMaps(SearchResponse response, Set<String> returnTypes) {
        return QueryResult.builder()
                .searchHits(getListHits(response, returnTypes))
                .totalHits(response.getHits().getTotalHits().value)
                .build();
    }

    private List<Map<String, Object>> getListHits(SearchResponse response, Set<String> returnTypes) {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        Arrays.asList(hits).forEach(hit-> {
            Map<String, Object> source = hit.getSourceAsMap();
            // codeless mapper to match with fields in graph.ql file
            Map<String, Object> returnMap = parseReturnMap(returnTypes,source);
            if (returnMap.size() > 0) result.add(returnMap);
        });
        return result;
    }

    private Map<String, Object> parseReturnMap(Set<String> returnTypes, Map<String, Object> source) {
        return returnTypes.stream()
                .filter(source::containsKey)
                .collect(HashMap::new, (k,v)->k.put(v, source.get(v)), HashMap::putAll);
    }
}
