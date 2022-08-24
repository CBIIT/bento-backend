package gov.nih.nci.bento.model.search.mapper;

import gov.nih.nci.bento.model.search.query.QueryResult;

import java.util.Set;

public interface TypeMapperService {

    TypeMapper<QueryResult> getQueryResult(Set<String> returnTypes);
}