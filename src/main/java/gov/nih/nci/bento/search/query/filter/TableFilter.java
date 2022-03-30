package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.TableParam;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.search.builder.SearchSourceBuilder;

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
                        getSortType(param),
                        tableParam.getSortDirection())
                .size(tableParam.getPageSize());
    }

    private String getSortType(FilterParam param) {
        TableParam tableParam = param.getTableParam();
        if (param.getCustomOrderBy() != null && !param.getCustomOrderBy().equals("")) return param.getCustomOrderBy();
        return tableParam.getOrderBy().equals("") ? param.getDefaultSortField() : tableParam.getOrderBy();
    }
}
