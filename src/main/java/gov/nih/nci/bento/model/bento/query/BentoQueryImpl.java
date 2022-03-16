package gov.nih.nci.bento.model.bento.query;

import gov.nih.nci.bento.classes.FilterParam;
import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.constants.Const;
import gov.nih.nci.bento.search.query.filter.AggregationFilter;
import gov.nih.nci.bento.search.query.filter.RangeFilter;
import gov.nih.nci.bento.search.query.filter.SearchCountFilter;
import gov.nih.nci.bento.search.query.filter.SubAggregationFilter;
import gov.nih.nci.bento.search.result.TypeMapperImpl;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Map;

@RequiredArgsConstructor
public class BentoQueryImpl implements BentoQuery {

    public final TypeMapperImpl typeMapper;

    @Override
    public MultipleRequests findNumberOfPrograms() {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_PROGRAMS)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.PROGRAMS)
                        .source(new SearchSourceBuilder().size(0)))
                .typeMapper(typeMapper.getIntTotal()).build();
    }

    @Override
    public MultipleRequests findNumberOfStudies() {
        return MultipleRequests.builder()
                .name(BentoGraphQLKEYS.NO_OF_STUDIES)
                .request(new SearchRequest()
                        .indices(Const.BENTO_INDEX.STUDIES)
                        .source(new SearchSourceBuilder().size(0)))
                .typeMapper(typeMapper.getIntTotal()).build();
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
                        .source(new SearchCountFilter(
                                FilterParam.builder()
                                        .args(args)
                                        .build())
                                .getSourceFilter()
                        ))
                .typeMapper(typeMapper.getIntTotal()).build();
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
