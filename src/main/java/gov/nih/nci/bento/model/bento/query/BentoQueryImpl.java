package gov.nih.nci.bento.model.bento.query;

import gov.nih.nci.bento.classes.*;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.filter.*;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import gov.nih.nci.bento.utility.StrUtil;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class BentoQueryImpl implements BentoQuery {

    public final TypeMapperImpl typeMapper;

    @Override
    public MultipleRequests findNumberOfPrograms(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_PROGRAMS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.PROGRAM + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregateTotalCnt()).build();
    }

    @Override
    public MultipleRequests findNumberOfStudies(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_STUDIES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregateTotalCnt()).build();
    }

    @Override
    public MultipleRequests findNumberOfSubjects(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_SUBJECTS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new SearchCountFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getIntTotal()).build();
    }

    @Override
    public MultipleRequests findNumberOfSamples(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_SAMPLES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SAMPLES)
                        .source(new SearchCountFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getIntTotal()).build();
    }

    @Override
    public MultipleRequests findNumberOfLabProcedures(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_LAB_PROCEDURES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.LAB_PROCEDURES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregateTotalCnt()).build();
    }

    @Override
    public MultipleRequests findNumberOfFiles(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_FILES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.FILES)
                        .source(new SearchCountFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getIntTotal()).build();
    }

    @Override
    public MultipleRequests findSubjectCntProgram(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_PROGRAM)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.PROGRAM + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntProgram(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PROGRAM)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.PROGRAM + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntStudy(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_STUDY)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntStudy(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_STUDY)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.STUDIES + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntDiagnoses(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_DIAGNOSES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.DIAGNOSES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntDiagnoses(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_DIAGNOSIS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.DIAGNOSES + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntRecurrence(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_RECURRENCE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntRecurrence(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_RECURRENCE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.RC_SCORES + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntTumorSize(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_TUMOR_SIZE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.TUMOR_SIZES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntTumorSize(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TUMOR_SIZE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.TUMOR_SIZES + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntTumorGrade(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_TUMOR_GRADE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.TUMOR_GRADES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntTumorGrade(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TUMOR_GRADE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.TUMOR_GRADES + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntErGrade(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_ER_STATUS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.ER_STATUS + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntErGrade(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_ER_STATUS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.ER_STATUS + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntPrStatus(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_PR_STATUS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.PR_STATUS + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntPrStatus(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PR_STATUS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.PR_STATUS + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntChemo(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_CHEMO)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.CHEMO_REGIMEN + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntChemo(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_PR_CHEMMO)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.CHEMO_REGIMEN + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntEndoTherapy(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_ENDO_THERAPY)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.ENDO_THERAPIES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntEndoTherapy(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_ENDO_THERAPY)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.ENDO_THERAPIES + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntMenoTherapy(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_MENO_THERAPY)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.MENO_STATUS + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntMenoTherapy(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_MENO_STATUS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.MENO_STATUS + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntTissueType(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_TISSUE_TYPE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.TISSUE_TYPE + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntTissueType(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TISSUE_TYPE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.TISSUE_TYPE + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntTissueComposition(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_TISSUE_COMPOSITION)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.COMPOSITION + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntTissueComposition(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_TISSUE_COMPOSITION)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.COMPOSITION + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntFileAssociation(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_FILE_ASSOCI)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.FILES)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.ASSOCIATION + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntFileAssociation(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_FILE_ASSOCIATION)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.FILES)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.ASSOCIATION + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findSubjectCntFileType(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_FILE_TYPE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.FILES)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.FILE_TYPE + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntFileType(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_FILE_TYPE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.FILES)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.FILE_TYPE + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findNumberOfArms(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_ARMS_PROGRAM)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new SubAggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.PROGRAM + Const.ES_UNITS.KEYWORD)
                                        .subAggSelectedField(Const.BENTO_FIELDS.STUDY_ACRONYM + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getArmProgram()).build();
    }

    @Override
    public MultipleRequests findSubjectCntLabProcedures(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.SUBJECT_COUNT_LAB_PROCEDURES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.LAB_PROCEDURES + Const.ES_UNITS.KEYWORD)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntLabProcedures(Map<String, Object> args) {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_LAB_PROCEDURES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new AggregationFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.LAB_PROCEDURES + Const.ES_UNITS.KEYWORD)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getAggregate()).build();
    }

    @Override
    public MultipleRequests findFilterSubjectCntByAge(Map<String, Object> args) {
        // Range Query
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.FILTER_SUBJECT_CNT_BY_AGE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(new RangeFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .selectedField(Const.BENTO_FIELDS.AGE_AT_INDEX)
                                        .isExcludeFilter(true)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getRange()).build();
    }

    @Override
    public MultipleRequests findGlobalSearchSubject(AbstractQueryParam param) {
        TableParam tableParam = param.getTableParam();
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(tableParam.getPageSize())
                .from(tableParam.getOffSet())
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM)
                .query(
                        addConditionalQuery(
                                new BoolQueryBuilder()
                                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.SUBJECT_ID_GS + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.DIGNOSIS_GS + Const.ES_UNITS.KEYWORD, "*" + param.getSearchText()+ "*")),
                                // Set Conditional Integer Query
                                QueryBuilders.termQuery(Const.BENTO_FIELDS.AGE_AT_INDEX,StrUtil.getIntText(param.getSearchText())))
                );
        return MultipleRequests.builder()
                .name(Const.BENTO_FIELDS.GLOBAL_SEARCH_SUBJECTS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SUBJECTS)
                        .source(builder))
                .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                        Const.BENTO_FIELDS.TYPE,
                        Const.BENTO_FIELDS.PROGRAM_ID,
                        Const.BENTO_FIELDS.SUBJECT_ID,
                        Const.BENTO_FIELDS.PROGRAM,
                        Const.BENTO_FIELDS.STUDY_ACRONYM,
                        Const.BENTO_FIELDS.DIAGNOSES,
                        Const.BENTO_FIELDS.AGE
                ))).build();
    }

    @Override
    public MultipleRequests findGlobalSearchSample(AbstractQueryParam param) {
        TableParam tableParam = param.getTableParam();

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(tableParam.getPageSize())
                .from(tableParam.getOffSet())
                .sort(Const.BENTO_FIELDS.SUBJECT_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.SAMPLE_ID_GS + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE_GS + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.TISSUE_TYPE_GS + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                );
        return MultipleRequests.builder()
                .name(Const.BENTO_FIELDS.GLOBAL_SEARCH_SAMPLE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.SAMPLES)
                        .source(builder))
                .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                        Const.BENTO_FIELDS.TYPE,
                        Const.BENTO_FIELDS.PROGRAM_ID,
                        Const.BENTO_FIELDS.SUBJECT_ID,
                        Const.BENTO_FIELDS.SAMPLE_ID,
                        Const.BENTO_FIELDS.DIAGNOSES,
                        Const.BENTO_FIELDS.SAMPLE_ANATOMIC_SITE,
                        Const.BENTO_FIELDS.TISSUE_TYPE
                ))).build();
    }

    @Override
    public MultipleRequests findGlobalSearchProgram(AbstractQueryParam param) {
        TableParam tableParam = param.getTableParam();
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(tableParam.getPageSize())
                .from(tableParam.getOffSet())
                .sort(Const.BENTO_FIELDS.PROGRAM_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROGRAM_ID + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROGRAM_CODE + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROGRAM_NAME, "*" + param.getSearchText() + "*"))
                );

        return MultipleRequests.builder()
                .name(Const.BENTO_FIELDS.GLOBAL_SEARCH_PROGRAM)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.PROGRAMS)
                        .source(builder))
                .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                        Const.BENTO_FIELDS.TYPE,
                        Const.BENTO_FIELDS.PROGRAM_CODE,
                        Const.BENTO_FIELDS.PROGRAM_ID,
                        Const.BENTO_FIELDS.PROGRAM_NAME
                ))).build();
    }

    @Override
    public MultipleRequests findGlobalSearchStudy(AbstractQueryParam param) {
        TableParam tableParam = param.getTableParam();
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(tableParam.getPageSize())
                .from(tableParam.getOffSet())
                .sort(Const.BENTO_FIELDS.STUDY_ID_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_ID + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_NAME + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.STUDY_TYPE + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                );
        return MultipleRequests.builder()
                .name(Const.BENTO_FIELDS.GLOBAL_SEARCH_STUDIES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.STUDIES)
                        .source(builder))
                .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                        Const.BENTO_FIELDS.TYPE,
                        Const.BENTO_FIELDS.PROGRAM_ID,
                        Const.BENTO_FIELDS.STUDY_ID,
                        Const.BENTO_FIELDS.STUDY_TYPE,
                        Const.BENTO_FIELDS.STUDY_CODE,
                        Const.BENTO_FIELDS.STUDY_NAME
                ))).build();
    }

    @Override
    public MultipleRequests findGlobalSearchFile(AbstractQueryParam param) {
        TableParam tableParam = param.getTableParam();
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(tableParam.getPageSize())
                .from(tableParam.getOffSet())
                .sort(Const.BENTO_FIELDS.FILE_ID_NUM, SortOrder.DESC)
                .query(new BoolQueryBuilder()
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_ID_GS + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.FILE_NAME, "*" + param.getSearchText() + "*" ))
                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.FILE_FORMAT_GS, param.getSearchText()))
                );
        return MultipleRequests.builder()
                .name(Const.BENTO_FIELDS.GLOBAL_SEARCH_FILE)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.FILES)
                        .source(builder))
                .typeMapper(typeMapper.getDefaultReturnTypes(Set.of(
                        Const.BENTO_FIELDS.TYPE,
                        Const.BENTO_FIELDS.PROGRAM_ID,
                        Const.BENTO_FIELDS.SUBJECT_ID,
                        Const.BENTO_FIELDS.SAMPLE_ID,
                        Const.BENTO_FIELDS.FILE_NAME,
                        Const.BENTO_FIELDS.FILE_FORMAT,
                        Const.BENTO_FIELDS.FILE_ID
                ))).build();
    }

    @Override
    public MultipleRequests findGlobalSearchModel(AbstractQueryParam param) {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .size(Const.ES_UNITS.MAX_SIZE)
                .from(0)
                .sort(Const.BENTO_FIELDS.PROGRAM_KW + Const.ES_UNITS.KEYWORD, SortOrder.DESC)
                .query(
                        addConditionalQuery(
                                new BoolQueryBuilder()
                                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.VALUE + Const.ES_UNITS.KEYWORD, "*" + param.getSearchText() + "*"))
                                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_NAME + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.PROPERTY_TYPE + Const.ES_UNITS.KEYWORD, param.getSearchText()))
                                        .should(QueryBuilders.wildcardQuery(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION + ".keyword", "*" + param.getSearchText() + "*"))
                                        .should(QueryBuilders.termQuery(Const.BENTO_FIELDS.NODE_NAME + Const.ES_UNITS.KEYWORD, param.getSearchText())),
                                // Set Conditional Bool Query
                                QueryBuilders.matchQuery(Const.BENTO_FIELDS.PROPERTY_REQUIRED,StrUtil.getBoolText(param.getSearchText())))
                ).highlighter(
                        new HighlightBuilder()
                                // Index model_properties
                                .field(Const.BENTO_FIELDS.PROPERTY_NAME)
                                .field(Const.BENTO_FIELDS.PROPERTY_DESCRIPTION)
                                .field(Const.BENTO_FIELDS.PROPERTY_TYPE)
                                .field(Const.BENTO_FIELDS.PROPERTY_REQUIRED)
                                // Index model_values
                                .field(Const.BENTO_FIELDS.VALUE)
                                // Index model_nodes
                                .field(Const.BENTO_FIELDS.NODE_NAME)
                                .preTags("")
                                .postTags("")
                                .fragmentSize(1)
                );
        return MultipleRequests.builder()
                .name(Const.BENTO_FIELDS.GLOBAL_SEARCH_MODEL)
                .request(new SearchRequest()
                        .indices(new String[]{Const.BENTO_INDEX.MODEL_PROPERTIES, Const.BENTO_INDEX.MODEL_VALUES, Const.BENTO_INDEX.MODEL_NODES})
                        .source(builder))
                .typeMapper(typeMapper.getMapWithHighlightedFields(Set.of(
                        Const.BENTO_FIELDS.TYPE,
                        Const.BENTO_FIELDS.NODE_NAME,
                        Const.BENTO_FIELDS.PROPERTY_NAME,
                        Const.BENTO_FIELDS.PROPERTY_DESCRIPTION,
                        Const.BENTO_FIELDS.PROPERTY_TYPE,
                        Const.BENTO_FIELDS.PROPERTY_REQUIRED,
                        Const.BENTO_FIELDS.VALUE
                ))).build();
    }

    @Override
    public MultipleRequests findGlobalSearchAboutPage(AbstractQueryParam param) {
        // Set Filter
        BoolQueryBuilder bool = new BoolQueryBuilder();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        bool.should(QueryBuilders.matchQuery(Const.BENTO_FIELDS.CONTENT_PARAGRAPH, param.getSearchText()));
        builder.query(bool);

        SearchRequest request = new SearchRequest();
        request.indices(Const.BENTO_INDEX.ABOUT);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(Const.BENTO_FIELDS.CONTENT_PARAGRAPH);
        highlightBuilder.preTags(Const.ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        highlightBuilder.postTags(Const.ES_UNITS.GS_HIGHLIGHT_DELIMITER);
        builder.highlighter(highlightBuilder);
        request.source(builder);

        return MultipleRequests.builder()
                .name(Const.BENTO_FIELDS.GLOBAL_SEARCH_ABOUT)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.ABOUT)
                        .source(builder))
                .typeMapper(typeMapper.getHighLightFragments(Const.BENTO_FIELDS.CONTENT_PARAGRAPH,
                        (source, text) -> Map.of(
                                Const.BENTO_FIELDS.TYPE, Const.BENTO_FIELDS.ABOUT,
                                Const.BENTO_FIELDS.PAGE, source.get(Const.BENTO_FIELDS.PAGE),
                                Const.BENTO_FIELDS.TITLE,source.get(Const.BENTO_FIELDS.TITLE),
                                Const.BENTO_FIELDS.TEXT, text))).build();
    }

    // Add Conditional Query
    private BoolQueryBuilder addConditionalQuery(BoolQueryBuilder builder, QueryBuilder... query) {
        List<QueryBuilder> builders = Arrays.asList(query);
        builders.forEach(q->{
            if (q.getName().equals("match")) {
                MatchQueryBuilder matchQuery = getQuery(q);
                if (!matchQuery.value().equals("")) builder.should(q);
            } else if (q.getName().equals("term")) {
                TermQueryBuilder termQuery = getQuery(q);
                if (!termQuery.value().equals("")) builder.should(q);
            }
        });
        return builder;
    }

    @SuppressWarnings("unchecked")
    private <T> T getQuery(QueryBuilder q) {
        String queryType = q.getName();
        return (T) q.queryName(queryType);
    }

    static class BentoGraphQLKEYS {
        static final String NO_OF_PROGRAMS = "numberOfPrograms";
        static final String NO_OF_STUDIES = "numberOfStudies";
        static final String NO_OF_LAB_PROCEDURES = "numberOfLabProcedures";
        static final String NO_OF_SUBJECTS = "numberOfSubjects";
        static final String NO_OF_SAMPLES = "numberOfSamples";
        static final String NO_OF_FILES = "numberOfFiles";
        static final String NO_OF_ARMS_PROGRAM = "armsByPrograms";
        static final String SUBJECT_COUNT_PROGRAM = "subjectCountByProgram";
        static final String SUBJECT_COUNT_STUDY = "subjectCountByStudy";
        static final String SUBJECT_COUNT_DIAGNOSES = "subjectCountByDiagnoses";
        static final String SUBJECT_COUNT_RECURRENCE = "subjectCountByRecurrenceScore";
        static final String SUBJECT_COUNT_TUMOR_SIZE = "subjectCountByTumorSize";
        static final String SUBJECT_COUNT_TUMOR_GRADE = "subjectCountByTumorGrade";
        static final String SUBJECT_COUNT_ER_STATUS = "subjectCountByErStatus";
        static final String SUBJECT_COUNT_PR_STATUS = "subjectCountByPrStatus";
        static final String SUBJECT_COUNT_CHEMO = "subjectCountByChemotherapyRegimen";
        static final String SUBJECT_COUNT_ENDO_THERAPY = "subjectCountByEndocrineTherapy";
        static final String SUBJECT_COUNT_MENO_THERAPY = "subjectCountByMenopauseStatus";
        static final String SUBJECT_COUNT_TISSUE_TYPE = "subjectCountByTissueType";
        static final String SUBJECT_COUNT_TISSUE_COMPOSITION = "subjectCountByTissueComposition";
        static final String SUBJECT_COUNT_FILE_ASSOCI = "subjectCountByFileAssociation";

        static final String SUBJECT_COUNT_FILE_TYPE = "subjectCountByFileType";
        static final String FILTER_SUBJECT_CNT_PROGRAM = "filterSubjectCountByProgram";
        static final String FILTER_SUBJECT_CNT_STUDY = "filterSubjectCountByStudy";
        static final String FILTER_SUBJECT_CNT_DIAGNOSIS = "filterSubjectCountByDiagnoses";
        static final String FILTER_SUBJECT_CNT_RECURRENCE = "filterSubjectCountByRecurrenceScore";
        static final String FILTER_SUBJECT_CNT_TUMOR_SIZE = "filterSubjectCountByTumorSize";
        static final String FILTER_SUBJECT_CNT_TUMOR_GRADE = "filterSubjectCountByTumorGrade";
        static final String FILTER_SUBJECT_CNT_ER_STATUS = "filterSubjectCountByErStatus";
        static final String FILTER_SUBJECT_CNT_PR_STATUS = "filterSubjectCountByPrStatus";
        static final String FILTER_SUBJECT_CNT_PR_CHEMMO = "filterSubjectCountByChemotherapyRegimen";
        static final String FILTER_SUBJECT_CNT_ENDO_THERAPY = "filterSubjectCountByEndocrineTherapy";
        static final String FILTER_SUBJECT_CNT_MENO_STATUS = "filterSubjectCountByMenopauseStatus";
        static final String FILTER_SUBJECT_CNT_TISSUE_TYPE = "filterSubjectCountByTissueType";
        static final String FILTER_SUBJECT_CNT_TISSUE_COMPOSITION = "filterSubjectCountByTissueComposition";
        static final String FILTER_SUBJECT_CNT_FILE_ASSOCIATION = "filterSubjectCountByFileAssociation";
        static final String FILTER_SUBJECT_CNT_FILE_TYPE = "filterSubjectCountByFileType";
        static final String FILTER_SUBJECT_CNT_BY_AGE = "filterSubjectCountByAge";

        static final String SUBJECT_COUNT_LAB_PROCEDURES = "subjectCountByLabProcedures";
        static final String FILTER_SUBJECT_CNT_LAB_PROCEDURES = "filterSubjectCountByLabProcedures";

        static final String PROGRAM_COUNT = "program_count";
        static final String PROGRAMS = "programs";
        static final String STUDY_COUNT = "study_count";
        static final String STUDIES = "studies";
        static final String SUBJECT_COUNT = "subject_count";
        static final String SUBJECTS = "subjects";
        static final String SAMPLE_COUNT = "sample_count";
        static final String SAMPLES = "samples";
        static final String FILE_COUNT = "file_count";
        static final String FILES = "files";
        static final String ABOUT_COUNT = "about_count";
        static final String ABOUT_PAGE = "about_page";
        static final String MODEL_COUNT = "model_count";
        static final String MODEL = "model";

    }
}
