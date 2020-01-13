package gov.nih.nci.icdc.model;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class Mocker {

	private String dashboard;
	private String programs;
	private String programStudy;
	private String studies;
	private String studyDetail;
	private String cases;
	private String caseDetail;
	private String landing;

	public Mocker() {
		this.dashboard = this.getResource("mock_data/dashboard.json");
		this.programs = this.getResource("mock_data/programs.json");
		this.programStudy = this.getResource("mock_data/program_study.json");
		this.studies = this.getResource("mock_data/studies.json");
		this.studyDetail = this.getResource("mock_data/study_detail.json");
		this.cases = this.getResource("mock_data/cases.json");
		this.caseDetail = this.getResource("mock_data/case_detail.json");
		this.landing = this.getResource("mock_data/landing.json");
	}

	

	public String getDashboard() {
		return dashboard;
	}



	public void setDashboard(String dashboard) {
		this.dashboard = dashboard;
	}



	public String getPrograms() {
		return programs;
	}



	public void setPrograms(String programs) {
		this.programs = programs;
	}



	public String getProgramStudy() {
		return programStudy;
	}



	public void setProgramStudy(String programStudy) {
		this.programStudy = programStudy;
	}



	public String getStudies() {
		return studies;
	}



	public void setStudies(String studies) {
		this.studies = studies;
	}



	public String getStudyDetail() {
		return studyDetail;
	}



	public void setStudyDetail(String studyDetail) {
		this.studyDetail = studyDetail;
	}



	public String getCases() {
		return cases;
	}



	public void setCases(String cases) {
		this.cases = cases;
	}



	public String getCaseDetail() {
		return caseDetail;
	}



	public void setCaseDetail(String caseDetail) {
		this.caseDetail = caseDetail;
	}



	public String getLanding() {
		return landing;
	}



	public void setLanding(String landing) {
		this.landing = landing;
	}



	private String getResource(String filePath) {

		StringBuilder sb = new StringBuilder();
		URL url = Resources.getResource(filePath);
		String sdl = "";
		try {
			sdl = Resources.toString(url, Charsets.UTF_8);
			sb.append(sdl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();

	}

}
