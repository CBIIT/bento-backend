package gov.nih.nci.bento.service;

import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Service
public class RedisFilterDataFetcher {
    private static final Logger logger = LogManager.getLogger(RedisFilterDataFetcher.class);

    @Autowired
    private RedisService redisService;

    public RuntimeWiring buildRuntimeWiring(){
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("redisFilter", getFilterResultsDataFetcher())
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

    private Map<String, Object> filter(Map<String, String[]> variables) {
        Map<String, String[]> categories = new HashMap<>();
        ArrayList<String> errors = new ArrayList<>();
        Map<String, String> paramsMapping = redisService.getParameterMappings();
        HashSet<String> filterGroups = new HashSet<>(Arrays.asList(redisService.getGroups()));
        for (String key : variables.keySet()) {
            String[] value = variables.get(key);
            List<String> values = Arrays.asList(value);
            if (!values.isEmpty()) {
                if (paramsMapping.containsKey(key)){
                    String group = paramsMapping.get(key);
                    if (filterGroups.contains(group)) {
                        categories.put(group, values.toArray(new String[0]));
                    } else {
                        errors.add(String.format("%s is not a valid filter group", group));
                    }
                }
                else{
                    errors.add(String.format("Could not find a group mapping for the parameter %s", key));
                }
            }
        }
        if (!errors.isEmpty()){
            errors.add("The following are the filter groups initialized in this instance: "+String.join(", ", filterGroups));
            errors.add("The following parameters have mappings in this instance: "+String.join(", ", paramsMapping.keySet()));
        }

        ArrayList<String> unionKeys = new ArrayList<>();
        for (String category : categories.keySet()){
            String unionKey = category+"_union";
            String[] filters = categories.get(category);
            for (int i = 0; i < filters.length; i++) {
                //replace spaces with underscore and format key as "category:filter"
                filters[i] = category + ":" + filters[i].replace(" ", "_");
            }
            redisService.unionStore(unionKey, filters);
            unionKeys.add(unionKey);
        }
        String[] intersection;
        if (unionKeys.size() == 0) {
            intersection = redisService.getCachedSet("all").toArray(new String[0]);
        } else {
            intersection = redisService.getIntersection(unionKeys.toArray(new String[0])).toArray(new String[0]);
        }

        for(String error : errors){
            logger.error(error);
        }

        Map<String, Object> output = new HashMap<>();
        output.put("numberOfSubjects", intersection.length);
        output.put("subjectIds", intersection);
        return output;
    }

}
