package gov.nih.nci.bento.model.search.mapper;

import gov.nih.nci.bento.model.search.query.QueryResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TypeMapperService {

    TypeMapper<QueryResult> getQueryResult(Set<String> returnTypes);
    TypeMapper<List<Map<String, Object>>> getList(Set<String> returnTypes);
}