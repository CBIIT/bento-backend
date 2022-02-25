package gov.nih.nci.bento.utility;


import org.elasticsearch.search.sort.SortOrder;

public class ElasticUtility {

    public static SortOrder getSortType(String sort) {
        if (sort==null) return SortOrder.DESC;
        return sort.equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
    }

}