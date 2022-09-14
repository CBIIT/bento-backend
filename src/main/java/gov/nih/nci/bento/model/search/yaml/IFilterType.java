package gov.nih.nci.bento.model.search.yaml;

import gov.nih.nci.bento.model.search.query.QueryParam;
import gov.nih.nci.bento.model.search.yaml.filter.YamlQuery;
import org.opensearch.search.builder.SearchSourceBuilder;

@FunctionalInterface
public interface IFilterType {
    SearchSourceBuilder getQueryFilter(QueryParam param, YamlQuery query);
}