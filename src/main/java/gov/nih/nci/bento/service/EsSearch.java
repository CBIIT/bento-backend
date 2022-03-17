package gov.nih.nci.bento.service;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.search.result.TypeMapper;
import graphql.schema.DataFetchingEnvironment;
import org.elasticsearch.action.search.SearchRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EsSearch {
    Map<String, Object> elasticMultiSend(List<MultipleRequests> requests) throws IOException;
    <T> T elasticSend(SearchRequest request, TypeMapper mapper) throws IOException;
    QueryParam CreateQueryParam(DataFetchingEnvironment env);
}
