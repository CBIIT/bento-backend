package gov.nih.nci.bento.model.search.yaml.type;

import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.model.search.MultipleRequests;
import gov.nih.nci.bento.model.search.query.QueryParam;
import gov.nih.nci.bento.model.search.query.QueryResult;
import gov.nih.nci.bento.model.search.yaml.IFilterType;
import gov.nih.nci.bento.model.search.yaml.ITypeQuery;
import gov.nih.nci.bento.model.search.yaml.SingleTypeQuery;
import gov.nih.nci.bento.model.search.yaml.filter.YamlQuery;
import gov.nih.nci.bento.service.ESService;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.opensearch.action.search.SearchRequest;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
@RequiredArgsConstructor
public class SingleTypeYaml extends AbstractYamlType {

    private final ESService esService;

    private List<YamlQuery> readYamlFile() throws IOException {
        Yaml yaml = new Yaml(new Constructor(SingleTypeQuery.class));
        SingleTypeQuery singleTypeQuery = yaml.load(new ClassPathResource(Const.YAML_QUERY.FILE_NAMES_BENTO.SINGLE).getInputStream());
        return singleTypeQuery.getQueries();
    }

    private Object multipleSend(YamlQuery query, QueryParam param, ITypeQuery iTypeQuery, IFilterType iFilterType) throws IOException {
        Map<String, QueryResult> multipleSendResult = esService.elasticMultiSend(
                List.of(MultipleRequests.builder()
                        .name(query.getName())
                        .request(new SearchRequest()
                        .indices(query.getIndex())
                        .source(iFilterType.getQueryFilter(param, query)))
                        .typeMapper(iTypeQuery.getReturnType(param, query)).build()));
        return multipleSendResult.get(query.getName());
    }

    @Override
    public void createSearchQuery(Map<String, DataFetcher> resultMap, ITypeQuery iTypeQuery, IFilterType iFilterType) throws IOException {
        readYamlFile().forEach(query->
                resultMap.put(query.getName(), env -> multipleSend(query, createQueryParam(env), iTypeQuery, iFilterType))
        );
    }
}
