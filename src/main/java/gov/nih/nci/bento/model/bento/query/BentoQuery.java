package gov.nih.nci.bento.model.bento.query;

import gov.nih.nci.bento.classes.MultipleRequests;
import gov.nih.nci.bento.classes.QueryParam;

import java.util.Map;

public interface BentoQuery {

    MultipleRequests findNumberOfPrograms(Map<String, Object> args);

    MultipleRequests findNumberOfStudies(Map<String, Object> args);

    MultipleRequests findNumberOfSubjects(Map<String, Object> args);

    MultipleRequests findNumberOfSamples(Map<String, Object> args);

    MultipleRequests findNumberOfLabProcedures(Map<String, Object> args);

    MultipleRequests findNumberOfFiles(Map<String, Object> args);

    MultipleRequests findSubjectCntProgram(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntProgram(Map<String, Object> args);

    MultipleRequests findSubjectCntStudy(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntStudy(Map<String, Object> args);

    MultipleRequests findSubjectCntDiagnoses(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntDiagnoses(Map<String, Object> args);

    MultipleRequests findSubjectCntRecurrence(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntRecurrence(Map<String, Object> args);

    MultipleRequests findSubjectCntTumorSize(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntTumorSize(Map<String, Object> args);

    MultipleRequests findSubjectCntTumorGrade(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntTumorGrade(Map<String, Object> args);

    MultipleRequests findSubjectCntErGrade(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntErGrade(Map<String, Object> args);

    MultipleRequests findSubjectCntPrStatus(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntPrStatus(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntChemo(Map<String, Object> args);

    MultipleRequests findSubjectCntChemo(Map<String, Object> args);

    MultipleRequests findSubjectCntEndoTherapy(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntEndoTherapy(Map<String, Object> args);

    MultipleRequests findSubjectCntMenoTherapy(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntMenoTherapy(Map<String, Object> args);

    MultipleRequests findSubjectCntTissueType(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntTissueType(Map<String, Object> args);

    MultipleRequests findSubjectCntTissueComposition(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntTissueComposition(Map<String, Object> args);

    MultipleRequests findSubjectCntFileAssociation(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntFileAssociation(Map<String, Object> args);

    MultipleRequests findSubjectCntFileType(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntFileType(Map<String, Object> args);

    MultipleRequests findNumberOfArms(Map<String, Object> args);

    MultipleRequests findSubjectCntLabProcedures(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntLabProcedures(Map<String, Object> args);

    MultipleRequests findFilterSubjectCntByAge(Map<String, Object> args);
    // Global Search Start
    MultipleRequests findGlobalSearchSubject(QueryParam param);

    MultipleRequests findGlobalSearchSample(QueryParam param);

    MultipleRequests findGlobalSearchProgram(QueryParam param);

    MultipleRequests findGlobalSearchStudy(QueryParam param);

    MultipleRequests findGlobalSearchFile(QueryParam param);

    MultipleRequests findGlobalSearchModel(QueryParam param);

    MultipleRequests findGlobalSearchAboutPage(QueryParam param);
}
