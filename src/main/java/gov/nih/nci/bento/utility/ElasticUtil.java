package gov.nih.nci.bento.utility;

import gov.nih.nci.bento.constants.Const;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;

public class ElasticUtil {

    public static SortOrder getSortType(String sort) {
        if (sort==null) return SortOrder.DESC;
        return sort.equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
    }

    // TODO Remove For ICDC
    public static TermsAggregationBuilder getTermsAggregation(String field) {
        return AggregationBuilders
                .terms(Const.ES_PARAMS.TERMS_AGGS)
                .size(Const.ES_PARAMS.AGGS_SIZE)
                .field(field);
    }
}