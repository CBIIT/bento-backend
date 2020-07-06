// SubjectOverview
MATCH (ss:study_subject)
OPTIONAL MATCH (ss)-[*]->(p:program)
OPTIONAL MATCH (ss)-[*]->(s:study)
OPTIONAL MATCH (ss)<-[*]-(sf:stratification_factor)
OPTIONAL MATCH (ss)<-[*]-(d:diagnosis)
OPTIONAL MATCH (ss)<-[*]-(tp:therapeutic_procedure)
OPTIONAL MATCH (ss)<-[*]-(demo:demographic_data)
OPTIONAL MATCH (ss)<-[*]-(f:file)
OPTIONAL MATCH (ss)<-[*]-(sp:sample)
OPTIONAL MATCH (ss)<--()-->(lp:laboratory_procedure)
RETURN p.program_acronym AS program,
       s.study_acronym AS study_acronym,
       s.study_short_description AS study_short_description,
       s.study_acronym + ': ' + s.study_short_description AS study_info,
       ss.study_subject_id AS subject_id,
       ss.disease_subtype AS diagnosis,
       sf.grouped_recurrence_score AS recurrence_score,
       d.tumor_size_group AS tumor_size,
       d.tumor_grade AS tumor_grade,
       d.er_status AS er_status,
       d.pr_status AS pr_status,
       tp.chemotherapy_regimen AS chemotherapy,
       tp.endocrine_therapy_type AS endocrine_therapy,
       demo.menopause_status AS menopause_status,
       demo.age_at_index AS age_at_index,
       demo.survival_time AS survival_time,
       demo.survival_time_unit AS survival_time_unit,
       collect(f) AS files,
       collect(sp.sample_id) AS samples,
       collect(lp.laboratory_procedure_id) AS lab_procedures

// subjectsInList
MATCH (ss:study_subject)
  WHERE ss.study_subject_id IN $subject_ids
OPTIONAL MATCH (ss)-[*]->(p:program)
OPTIONAL MATCH (ss)-[*]->(s:study)
OPTIONAL MATCH (ss)<-[*]-(sf:stratification_factor)
OPTIONAL MATCH (ss)<-[*]-(d:diagnosis)
OPTIONAL MATCH (ss)<-[*]-(tp:therapeutic_procedure)
OPTIONAL MATCH (ss)<-[*]-(demo:demographic_data)
RETURN p.program_acronym AS program,
       s.study_acronym AS study_acronym,
       ss.study_subject_id AS subject_id,
       ss.disease_subtype AS diagnosis,
       sf.grouped_recurrence_score AS recurrence_score,
       d.tumor_size_group AS tumor_size,
       d.er_status AS er_status,
       d.pr_status AS pr_status,
       demo.age_at_index AS age_at_index,
       demo.survival_time AS survival_time,
       demo.survival_time_unit AS survival_time_unit

// filesOfSubjects
MATCH (ss:study_subject)<-[*]-(f:file), (parent)<--(f)
  WHERE ss.study_subject_id IN $subject_ids
RETURN ss.study_subject_id AS subject_id,
       f.file_name AS file_name,
       f.file_type AS file_type,
       head(labels(parent)) AS association,
       f.file_description AS file_description,
       f.file_format AS file_format,
       f.file_size AS file_size,
       f.file_id AS file_id,
       f.md5sum AS md5sum

// programDetail
MATCH (p:program {program_id: $program_id})
MATCH (p)<-[*]-(s:study)<-[*]-(ss:study_subject)
WITH s {.study_type, .study_acronym, .study_name, .study_full_description,
       num_subjects:count(DISTINCT ss.study_subject_id)} AS study, p
OPTIONAL MATCH (p)<-[*]-(ss:study_subject)
OPTIONAL MATCH (p)-[*]->(ins:institution)
OPTIONAL MATCH (p)<-[*]-(f:file)
RETURN p.program_acronym AS program_acronym,
       p.program_id AS program_id,
       p.program_name AS program_name,
       p.program_full_description AS program_full_description,
       ins.institution_name AS institution_name,
       p.program_external_url AS program_external_url,
       count(DISTINCT ss) AS num_subjects,
       count(DISTINCT f) AS num_files,
       collect(DISTINCT ss.disease_subtype) AS disease_subtypes,
       collect(DISTINCT study) AS studies

// subjectDetail
MATCH (ss:study_subject {study_subject_id: $subject_id})-[*]->(s:study)-[*]->(p:program)
OPTIONAL MATCH (ss)<-[*]-(demo:demographic_data)
OPTIONAL MATCH (ss)<-[*]-(diag:diagnosis)
OPTIONAL MATCH (ss)<-[*]-(pr:therapeutic_procedure)
OPTIONAL MATCH (ss)<-[*]-(fu:follow_up)
OPTIONAL MATCH (ss)<-[*]-(f:file)
OPTIONAL MATCH (parent)<--(f)
RETURN ss.study_subject_id AS subject_id,
       p.program_acronym AS program_acronym,
       s.study_acronym AS study_acronym,
       s.study_name AS study_name,
       demo.gender AS gender,
       demo.race AS race,
       demo.ethnicity AS ethnicity,
       demo.age_at_index AS age_at_index,
       demo.menopause_status AS menopause_status,
       demo.vital_status AS vital_status,
       demo.cause_of_death AS cause_of_death,
       ss.disease_type AS disease_type,
       ss.disease_subtype AS disease_subtype,
       diag.tumor_grade AS tumor_grade,
       diag.tumor_largest_dimension_diameter AS tumor_largest_dimension_diameter,
       diag.er_status AS er_status,
       diag.pr_status AS pr_status,
       diag.nuclear_grade AS nuclear_grade,
       diag.recurrence_score AS recurrence_score,
       pr.primary_surgical_procedure AS primary_surgical_procedure,
       pr.chemotherapy_regimen_group AS chemotherapy_regimen_group,
       pr.chemotherapy_regimen AS chemotherapy_regimen,
       pr.endocrine_therapy_type AS endocrine_therapy_type,
       fu.dfs_event_indicator AS dfs_event_indicator,
       fu.recurrence_free_indicator AS recurrence_free_indicator,
       fu.distant_recurrence_indicator AS distant_recurrence_indicator,
       fu.dfs_event_type AS dfs_event_type,
       fu.first_recurrence_type AS first_recurrence_type,
       fu.days_to_progression AS days_to_progression,
       fu.days_to_recurrence AS days_to_recurrence,
       collect(f {subject_id:ss.study_subject_id, .file_name, .file_type,
         association:head(labels(parent)), .file_description, .file_format, .file_size, .file_id, .md5sum}) AS files