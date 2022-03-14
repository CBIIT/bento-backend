package gov.nih.nci.bento.service;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.model.TypeMapper;
import org.elasticsearch.action.search.SearchRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EsSearch {
    Map<String, Object> elasticMultiSend(List<MultipleRequests> requests) throws IOException;
    <T> T elasticSend(Map<String, String> resultType, SearchRequest request, TypeMapper mapper) throws IOException;
}
