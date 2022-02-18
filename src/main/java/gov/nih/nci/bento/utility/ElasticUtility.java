package gov.nih.nci.bento.utility;

import co.elastic.clients.elasticsearch._types.SortOrder;

public class ElasticUtility {

    public static SortOrder getSortType(String sort) {
        if (sort==null) return SortOrder.Desc;
        return sort.equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
    }

}