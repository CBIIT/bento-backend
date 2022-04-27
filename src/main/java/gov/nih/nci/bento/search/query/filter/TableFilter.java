package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.TableParam;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import static gov.nih.nci.bento.utility.ElasticUtil.getPrioritySortType;
import static gov.nih.nci.bento.utility.ElasticUtil.getSortDirection;

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
                        getPrioritySortType(param),
                        getSortDirection(param))
                .size(tableParam.getPageSize());
    }
}
