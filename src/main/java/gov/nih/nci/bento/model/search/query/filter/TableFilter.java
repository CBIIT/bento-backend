package gov.nih.nci.bento.model.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.TableParam;
import gov.nih.nci.bento.model.search.query.QueryCreator;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class TableFilter extends AbstractFilter {

    public TableFilter(FilterParam param) {
        super(param);
    }

    @Override
    SearchSourceBuilder getFilter(FilterParam param, QueryCreator bentoParam) {
        TableParam tableParam = param.getTableParam();
        return new SearchSourceBuilder()
                .query(
                        bentoParam.getQuery()
                )
                .from(tableParam.getOffSet())
                .sort(
                        // TODO Consider Keyword OrderBY
                        tableParam.getOrderBy().equals("") ? param.getDefaultSortField() : tableParam.getOrderBy(),
                        tableParam.getSortDirection())
                .size(tableParam.getPageSize());
    }
}
