package gov.nih.nci.bento.service;

import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Service
public class ESFilterDataFetcher {
    private static final Logger logger = LogManager.getLogger(RedisFilterDataFetcher.class);

    @Autowired ESService esService;

    public RuntimeWiring buildRuntimeWiring(){
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("searchSubject2", getFilterResultsDataFetcher())
                )
                .build();
    }

    private DataFetcher getFilterResultsDataFetcher() {
        return env -> {
            Map<String, Object> args = env.getArguments();
            Map<String, String[]> variables = new HashMap<>();
            for (String key : args.keySet()) {
                variables.put(key, ((ArrayList<String>) args.get(key)).toArray(new String[0]));
            }
            return filter(variables);
        };
    }

    private Map<String, Object> filter(Map<String, String[]> variables) throws FilterException {
        return null;
    }

    private class FilterException extends Exception {
        private String message = "";

        FilterException(ArrayList<String> invalidGroups, ArrayList<String> invalidParams){
            if (!invalidGroups.isEmpty()){
                message = String.format("The following filter groups were not found by the initialization queries: %s",
                        String.join(", ",invalidGroups));
            }
            if (!invalidParams.isEmpty()){
                if (!message.isEmpty()){
                    message += "; ";
                }
                message += String.format("The following parameters are not mapped to a filter group: %s",
                        String.join(", ",invalidParams));
            }
        }

        @Override
        public String getMessage(){
            return message;
        }
    }
}
