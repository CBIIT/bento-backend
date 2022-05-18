package gov.nih.nci.bento.service;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.search.result.TypeMapper;
import graphql.schema.DataFetchingEnvironment;
import org.elasticsearch.action.search.SearchRequest;

import java.util.List;
import java.util.Map;

public interface EsSearch {
    Map<String, Object> elasticMultiSend(List<MultipleRequests> requests);
    <T> T elasticSend(SearchRequest request, TypeMapper<T> mapper);
    QueryParam CreateQueryParam(DataFetchingEnvironment env);
}
