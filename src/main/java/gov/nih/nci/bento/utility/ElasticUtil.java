package gov.nih.nci.bento.utility;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.TableParam;
import org.elasticsearch.search.sort.SortOrder;

public class ElasticUtil {

    public static SortOrder getSortType(String sort) {
        if (sort==null) return SortOrder.DESC;
        return sort.equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
    }

    public static SortOrder getSortDirection(FilterParam param) {
        TableParam tableParam = param.getTableParam();
        // If SortDirection Defined, Sort it by Custom
        return param.getSortDirection() !=null ? ElasticUtil.getSortType(param.getSortDirection()) : tableParam.getSortDirection();
    }

    public static String getAlternativeSortType(FilterParam param) {
        TableParam tableParam = param.getTableParam();
        // TODO CHECK
        if (param.getCustomOrderBy() != null && !param.getCustomOrderBy().equals("")) return param.getCustomOrderBy();
        return tableParam.getOrderBy().equals("") ? param.getDefaultSortField() : tableParam.getOrderBy();
    }
}