package gov.nih.nci.bento.search.query.filter;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.TableParam;
import gov.nih.nci.bento.search.query.QueryFactory;
import org.opensearch.search.builder.SearchSourceBuilder;

import static gov.nih.nci.bento.utility.ElasticUtil.getAlternativeSortType;
import static gov.nih.nci.bento.utility.ElasticUtil.getSortDirection;

public class PaginationFilter extends AbstractFilter {

    public PaginationFilter(FilterParam param) {
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
}
