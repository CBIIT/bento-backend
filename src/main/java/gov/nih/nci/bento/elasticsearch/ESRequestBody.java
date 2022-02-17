package gov.nih.nci.bento.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESRequestBody {

    private static final Logger logger = LogManager.getLogger(ESRequestBody.class);
    private static final String SIZE_KEY = "first";
    private static final String FROM_KEY = "offset";
    private static final String SORT_PROPERTY_KEY = "order_by";
    private static final String ORDER_KEY = "sort_direction";

    private int size = 10000;
    private int from = 0;
    private Query query;
    private Map<String, Aggregation> aggs;
    private ArrayList<Map<String, Map<String, String>>> sort  = new ArrayList<>();

    public ESRequestBody(Map<String, Object> params, List<String> countProperties) {
        query = new Query(params);
        size = 0;
        aggs = new HashMap<>();
        countProperties.forEach(x -> aggs.put(x, new Aggregation(x)));
    }

    public ESRequestBody(Map<String, Object> params, String defaultSortProperty) {
        Map<String, Object> filters = new HashMap<>();
        filters.putAll(params);
        if (params.containsKey(SIZE_KEY)){
            try{
                size = (int) params.get(SIZE_KEY);
            }
            catch (ClassCastException e) {
                logger.error(String.format("Unable to cast %s input to an int, this parameter will default to %d",
                        SIZE_KEY, size));
                logger.error(e);
            }
            filters.remove(SIZE_KEY);
        }
        if (params.containsKey(FROM_KEY)){
            try{
                from = (int) params.get(FROM_KEY);
            }
            catch (ClassCastException e) {
                logger.error(String.format("Unable to cast %s input to an int, this parameter will default to %d",
                        FROM_KEY, from));
                logger.error(e);
            }
            filters.remove(FROM_KEY);
        }
        String sortProperty = defaultSortProperty;
        if (params.containsKey(SORT_PROPERTY_KEY) && !((String) params.get(SORT_PROPERTY_KEY)).equals("")) {
            sortProperty = (String) params.get(SORT_PROPERTY_KEY);
        }
        String orderProperty = "asc";
        if (params.containsKey(ORDER_KEY) && ((String) params.get(ORDER_KEY)).toLowerCase().equals("desc")) {
            orderProperty = "desc";
        }
        sort.add(Map.of(sortProperty, Map.of("order", orderProperty)));
        filters.remove(SORT_PROPERTY_KEY);
        filters.remove(ORDER_KEY);
        query = new Query(filters);
    }

    class Query {
        private Bool bool;
        Query(Map<String, Object> params) {
            bool = new Bool(params);
        }
    }

    class Bool {
        private ArrayList<Map<String, Object>> filter;
        Bool(Map<String, Object> params) {
            filter = new ArrayList<>();
            for (String key : params.keySet()) {
                try{
                    Object value = params.get(key);
                    ArrayList<String> filterValues = (ArrayList<String>) value;
                    if (filterValues.size() > 0) {
                        filter.add(Map.of("terms", Map.of(key, value)));
                    }
                }
                catch (ClassCastException e) {
                    logger.warn(String.format("Parameter %s has a value that cannot be case to ArrayList<String>," +
                            "it will be excluded from the query", key));
                    logger.warn(e);
                }
            }
        }
    }

    class Aggregation {
        private Map<String, String> terms;
        Aggregation(String property) {
            terms = Map.of("field", property);
        }
    }
}




