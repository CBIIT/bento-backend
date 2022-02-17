package gov.nih.nci.bento.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gov.nih.nci.bento.elasticsearch.ESRequestBuilder;
import gov.nih.nci.bento.elasticsearch.ESResponseParser;
import gov.nih.nci.bento.service.ESService;
import gov.nih.nci.bento.service.ESServiceAlternate;
import graphql.schema.idl.RuntimeWiring;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class C3DCElasticSearchDataFetcher implements DataFetcher{
    private static final Logger logger = LogManager.getLogger(C3DCElasticSearchDataFetcher.class);

    // subjects index endpoints
    private static final String SUBJECTS_END_POINT = "/subject_characteristics/_search";
    private static final String SUBJECTS_COUNT_END_POINT = "/subject_characteristics/_count";
    // files index endpoints
    private static final String FILES_END_POINT = "/files/_search";
    private static final String FILES_COUNT_END_POINT = "/files/_count";

    @Autowired
    ESServiceAlternate esService;

    public RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("subjectOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return subjectOverview(args);
                        })
                        .dataFetcher("fileOverview", env -> {
                            Map<String, Object> args = env.getArguments();
                            return fileOverview(args);
                        })
                        .dataFetcher("searchSubjects", env -> {
                            Map<String, Object> args = env.getArguments();
                            return searchSubjects(args);
                        })
                )
                .build();
    }

    private List<Map<String, Object>> subjectOverview(Map<String, Object> params) throws IOException {
        Request request = ESRequestBuilder.buildFilterSearch(
                SUBJECTS_END_POINT,
                params,
                "pcdc_subject_id"
        );
        return ESResponseParser.getHits(esService.send(request));
    }

    private List<Map<String, Object>> fileOverview(Map<String, Object> params) throws IOException {
        Request request = ESRequestBuilder.buildFilterSearch(
                FILES_END_POINT,
                params,
                "file_name"
        );
        return ESResponseParser.getHits(esService.send(request));
    }

    private Map<String, Object> searchSubjects(Map<String, Object> params) throws IOException {
        List<String> countParams = List.of("program_id", "study_id", "pcdc_subject_id", "file_name");
        Request request = ESRequestBuilder.buildFilterCount(
                SUBJECTS_END_POINT,
                params,
                countParams
        );
        JsonObject response = esService.send(request);
        //todo test in debugger
        return null;
    }

}
