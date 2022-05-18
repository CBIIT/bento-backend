package gov.nih.nci.bento.model.bento;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;
import gov.nih.nci.bento.classes.QueryResult;
import gov.nih.nci.bento.classes.TableParam;
import gov.nih.nci.bento.constants.Const.BENTO_FIELDS;
import gov.nih.nci.bento.model.bento.query.BentoQuery;
import gov.nih.nci.bento.model.bento.query.BentoQueryImpl;
import gov.nih.nci.bento.search.datafetcher.DataFetcher;
import gov.nih.nci.bento.search.result.TypeMapperService;
import gov.nih.nci.bento.search.yaml.YamlQueryFactory;
import gov.nih.nci.bento.service.EsSearch;
import graphql.schema.idl.RuntimeWiring;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@RequiredArgsConstructor
public final class BentoEsSearch implements DataFetcher {

    private static final Logger logger = LogManager.getLogger(BentoEsSearch.class);
    private final EsSearch esService;
    private final TypeMapperService typeMapper;
    private BentoQuery bentoQuery;
    private YamlQueryFactory yamlQueryFactory;

    @PostConstruct
    public void init() {
        bentoQuery = new BentoQueryImpl(typeMapper);
        yamlQueryFactory = new YamlQueryFactory(esService, typeMapper);
    }

    @Override
    public RuntimeWiring buildRuntimeWiring() throws IOException {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("QueryType")
                        .dataFetcher("globalSearch", env ->
                                globalSearch(esService.CreateQueryParam(env))
                        )
                        .dataFetchers(yamlQueryFactory.createYamlQueries())
                )
                .build();
    }

    private List<?> checkEmptySearch(QueryParam param, List<Map<String, Object>> result) {
        return param.getSearchText().equals("") ? new ArrayList<>() : result;
    }

    private int checkEmptySearch(QueryParam param, int result) {
        return param.getSearchText().equals("") ? 0 :result;
    }

    private Map<String, Object> globalSearch(QueryParam param) {
        Map<String, Object> result = new HashMap<>();
        // Set Bool Filter
        List<MultipleRequests> requests = List.of(
                bentoQuery.findGlobalSearchSubject(param),
                bentoQuery.findGlobalSearchSample(param),
                bentoQuery.findGlobalSearchProgram(param),
                bentoQuery.findGlobalSearchStudy(param),
                bentoQuery.findGlobalSearchFile(param),
                bentoQuery.findGlobalSearchModel(param),
                bentoQuery.findGlobalSearchAboutPage(param)
        );

        Map<String, Object> multiResult = esService.elasticMultiSend(requests);
        QueryResult subjects = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SUBJECTS);
        result.put("subjects", checkEmptySearch(param, subjects.getSearchHits()));
        result.put("subject_count", checkEmptySearch(param, subjects.getTotalHits()));

        QueryResult samples = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_SAMPLE);
        result.put("samples", checkEmptySearch(param, samples.getSearchHits()));
        result.put("sample_count", checkEmptySearch(param, samples.getTotalHits()));

        QueryResult programs = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_PROGRAM);
        result.put("programs", checkEmptySearch(param, programs.getSearchHits()));
        result.put("program_count", checkEmptySearch(param, programs.getTotalHits()));

        QueryResult studies = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_STUDIES);
        result.put("studies", checkEmptySearch(param, studies.getSearchHits()));
        result.put("study_count", checkEmptySearch(param, studies.getTotalHits()));

        QueryResult files = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_FILE);
        result.put("files", checkEmptySearch(param, files.getSearchHits()));
        result.put("file_count", checkEmptySearch(param, files.getTotalHits()));

        QueryResult model = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_MODEL);
        result.put("model", checkEmptySearch(param, model.getSearchHits()));
        result.put("model_count", checkEmptySearch(param, model.getTotalHits()));

        QueryResult aboutPage = (QueryResult) multiResult.get(BENTO_FIELDS.GLOBAL_SEARCH_ABOUT);
        TableParam tableParam = param.getTableParam();
        result.put("about_count", checkEmptySearch(param, aboutPage.getSearchHits().size()));
        result.put("about_page", paginate(aboutPage.getSearchHits(), tableParam.getPageSize(), tableParam.getOffSet()));

        Set<String> combinedCategories = Set.of("model");
        for (String category: combinedCategories) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pagedCategory = paginate((List<Map<String, Object>>)result.get(category), tableParam.getPageSize(), tableParam.getOffSet());
            result.put(category, pagedCategory);
        }
        return result;
    }

    private <T> List<T> paginate(List<T> org, int pageSize, int offset) {
        List<T> result = new ArrayList<>();
        int size = org.size();
        if (offset <= size -1) {
            int end_index = offset + pageSize;
            if (end_index > size) end_index = size;
            result = org.subList(offset, end_index);
        }
        return result;
    }
}
