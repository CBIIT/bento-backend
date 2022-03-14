package gov.nih.nci.bento.service;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.model.ITypeMapper;
import org.elasticsearch.action.search.SearchRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IEsSearch {
    Map<String, Object> elasticMultiSend(List<MultipleRequests> requests) throws IOException;
    <T> T elasticSend(Map<String, String> resultType, SearchRequest request, ITypeMapper mapper) throws IOException;
}
