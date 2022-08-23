package gov.nih.nci.bento.model.search.filter;

import gov.nih.nci.bento.model.search.query.QueryFactory;
import gov.nih.nci.bento.utility.ElasticUtil;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

public class TableFilter extends AbstractFilter {

    public TableFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryFactory bentoParam) {
        TableParam tableParam = param.getTableParam();
        return new SearchSourceBuilder()
                .query(
                        bentoParam.getQuery()
                )
                .from(tableParam.getOffSet())
                .sort(
                        getAlternativeSortType(param),
                        getSortDirection(param))
                .size(tableParam.getPageSize());
    }

    private SortOrder getSortDirection(FilterParam param) {
        TableParam tableParam = param.getTableParam();
        // If SortDirection Defined, Sort it by Custom
        return param.getSortDirection() !=null ? ElasticUtil.getSortType(param.getSortDirection()) : tableParam.getSortDirection();
    }

    private String getAlternativeSortType(FilterParam param) {
        TableParam tableParam = param.getTableParam();
        // TODO CHECK
        if (param.getCustomOrderBy() != null && !param.getCustomOrderBy().equals("")) return param.getCustomOrderBy();
        return tableParam.getOrderBy().equals("") ? param.getDefaultSortField() : tableParam.getOrderBy();
    }
}
