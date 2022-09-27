package gov.nih.nci.bento.model.search.yaml;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.search.filter.DefaultFilter;
import gov.nih.nci.bento.model.search.filter.FilterParam;
import gov.nih.nci.bento.model.search.mapper.TypeMapperImpl;
import gov.nih.nci.bento.model.search.mapper.TypeMapperService;
import gov.nih.nci.bento.model.search.yaml.filter.YamlFilter;
import gov.nih.nci.bento.model.search.yaml.type.AbstractYamlType;
import gov.nih.nci.bento.model.search.yaml.type.SingleTypeYaml;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class YamlQueryFactory {

    private final ESService esService;
    private final TypeMapperService typeMapper = new TypeMapperImpl();
    private static final Logger logger = LogManager.getLogger(YamlQueryFactory.class);

    public Map<String, DataFetcher> createYamlQueries() throws IOException {
        logger.info("Loading Yaml File Queries");
        // Set Single Request API
        List<AbstractYamlType> yamlFileList = List.of(new SingleTypeYaml(esService));
        Map<String, DataFetcher> result = new HashMap<>();
        for (AbstractYamlType yamlFile : yamlFileList) {
            yamlFile.createSearchQuery(result, getReturnType(), getFilterType());
        }
        return result;
    }

    private ITypeQuery getReturnType() {
        return (param, query) -> {
            switch (query.getResult().getType()) {
            case Const.YAML_QUERY.RESULT_TYPE.OBJECT_ARRAY:
                return typeMapper.getList(param.getReturnTypes());
            default:
                throw new IllegalArgumentException(query.getResult().getType() + " is not correctly declared as a return type in yaml file. Please, correct it and try again.");
            }
        };
    }

    private IFilterType getFilterType() {
        return (param, query) -> {
            // Set Arguments
            YamlFilter filterType = query.getFilter();
            switch (filterType.getType()) {
                case Const.YAML_QUERY.FILTER.DEFAULT:
                    return new DefaultFilter(FilterParam.builder()
                            .args(param.getArgs())
                            .caseInsensitive(filterType.isCaseInsensitive())
                            .ignoreIfEmpty(filterType.getIgnoreIfEmpty()).build())
                            .getSourceFilter();
                default:
                    throw new IllegalArgumentException(filterType + " is not correctly declared as a filter type in yaml file. Please, correct it and try again.");
            }
        };
    }
}
