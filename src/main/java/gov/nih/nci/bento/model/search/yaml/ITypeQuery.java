package gov.nih.nci.bento.model.search.yaml;

import gov.nih.nci.bento.model.search.mapper.TypeMapper;
import gov.nih.nci.bento.model.search.query.QueryParam;
import gov.nih.nci.bento.model.search.yaml.filter.YamlQuery;

@FunctionalInterface
public interface ITypeQuery {
    TypeMapper getReturnType(QueryParam param, YamlQuery query);
}