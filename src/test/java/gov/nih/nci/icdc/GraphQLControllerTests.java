package gov.nih.nci.icdc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphQLControllerTests {
	
	@Autowired
	private WebApplicationContext context;
	
	private MockMvc mockMvc;

	@Rule
	public JUnitRestDocumentation jUnitRestDocumentation = new JUnitRestDocumentation();

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(documentationConfiguration(this.jUnitRestDocumentation)).build();
	}

	

	private void testGraphQLAPI(String parms) throws Exception {

		this.mockMvc
				.perform(RestDocumentationRequestBuilders.post("/v1/graphql/").content(parms)
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("data")))
				.andDo(document("{ClassName}/{methodName}"));
	}

	@Test
	public void testAPINumberOfStudies() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{ numberOfStudies }\"}");
	}

	@Test
	public void testAPINumberOfCases() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{ numberOfCases }\"}");
	}

	@Test
	public void testAPINumberOfSamples() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{ numberOfSamples }\"}");
	}

	@Test
	public void testAPINumberOfFiles() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{ numberOfFiles }\"}");
	}

	@Test
	public void testAPINumberOfBiospecimenAliquots() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{ numberOfAliquots }\"}");
	}

	@Test
	public void testAPIAllStudies() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{\n" + "   \n" + "\n" + "    studiesByProgram {\n" + "        program_id\n"
				+ "        clinical_study_designation\n" + "        clinical_study_name\n"
				+ "         clinical_study_type\n" + "         numberOfCases\n" + "       \n" + "    }\n" + "    \n"
				+ "\n" + "}\n" + "     \"}");
	}

	@Test
	public void testAPIAllCases() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{\n" + "     caseOverview\n" + "    {   case_id  \n"
				+ "        study_code   \n" + "        study_type   \n" + "        breed   \n"
				+ "        diagnosis   \n" + "        stage_of_disease   \n" + "        age   \n" + "        sex   \n"
				+ "        neutered_status \n" + "        \n" + "    }\n" + "} \"}");
	}

	@Test
	public void testAPIAllProgram() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{ \n" + 
				"   program\n" + 
				"  {    \n" + 
				"     program_name   \n" + 
				"     program_acronym    \n" + 
				"      program_external_url    \n" + 
				"       program_full_description    \n" + 
				"       program_short_description    \n" + 
				"       program_sort_order \n" + 
				"    }\n" + 
				"}      \"}");
	}
	

	@Test
	public void testAPICaseCountByBreed() throws Exception {

		this.testGraphQLAPI(
				"{\"query\":\" {\n" + "    caseCountByBreed{\n" + "    cases\n" + "    breed\n" + "  }\n" + "}\"}");
	}

	@Test
	public void testAPIcaseCountByDiagnosis() throws Exception {

		this.testGraphQLAPI("{\"query\":\"{\n" + "      caseCountByDiagnosis{\n" + "    cases\n" + "    diagnosis\n"
				+ "  }\n" + "} \"}");
	}

	@Test
	public void testAPICaseCountByStudyCode() throws Exception {

		this.testGraphQLAPI("{\"query\":\" {\n" + "      caseCountByStudyCode {\n" + "   cases\n" + "    study_code\n"
				+ "  }\n" + "}\"}");
	}

	@Test
	public void testAPICaseCountByGender() throws Exception {

		this.testGraphQLAPI(
				"{\"query\":\" {\n" + "      caseCountByGender {\n" + "   cases\n" + "    gender\n" + "  }\n" + "}\"}");
	}

	// Using program’s property(id)  to find related studies. 
	@Test
	public void testAPIStuidesOfAProgram() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				" studiesByProgramId(program_id: \\\"COP\\\") {\n" + 
				"   program_id\n" + 
				"        clinical_study_designation\n" + 
				"        numberOfCases\n" + 
				"        clinical_study_name\n" + 
				" }\n" + 
				"}\"}");
	}

	@Test
	public void testAPIStuidesOfProgram() throws Exception {

		this.testGraphQLAPI("{\"query\":\" {\n" + " studiesByProgram{\n" + "   program_id\n"
				+ "        clinical_study_designation\n" + "        numberOfCases\n" + "        clinical_study_name\n"
				+ " }\n" + "}\"}");
	}

	@Test
	public void testAPINumbersOfProgram() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" "
				+ "{\n" + 
				"sampleCountOfProgram(program_id: \\\"COP\\\") \n" + 
				"fileCountOfProgram(program_id: \\\"COP\\\") \n" + 
				"aliguotCountOfProgram(program_id: \\\"COP\\\") \n" + 
				"studyCountOfProgram(program_id: \\\"COP\\\") \n" + 
				"caseCountOfProgram(program_id: \\\"COP\\\") \n" + 
				"    \n" + 
				"}"
				+ "\"}");
	}
	
	//Using study’sto find related cases.    
	@Test
	public void testAPICasesOfStudies() throws Exception {
		this.testGraphQLAPI("{\"query\":\"  {\n" + 
				" casesByStudyId(study_id: \\\"COTC007B\\\") {\n" + 
				"   patient_id\n" + 
				"   patient_first_name\n" + 
				" }\n" + 
				"}\"}");
	}


	@Test
	public void testAPISamplesOfStudy() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				"    sampleCountOfStudy(study_code: \\\"COTC007B\\\")\n" + 
				"}\"}");
	
	}
	
	
	@Test
	public void testAPIFilesOfStudy() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				"    fileCountOfStudy(study_code: \\\"COTC007B\\\")\n" + 
				"}"
				+ "\"}");
	
	}
	
	
	@Test
	public void testAPIAliguotsOfStudy() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				"    aliguotCountOfStudy(study_code: \\\"COTC007B\\\")\n" + 
				"}"
				+ "\"}");
	
	}
	
	
	
	@Test
	public void testAPIStudyDetails() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				" study(clinical_study_designation:\\\"COTC007B\\\"){\n" + 
				"     clinical_study_id\n" + 
				"  clinical_study_designation\n" + 
				"  clinical_study_name\n" + 
				"  clinical_study_description\n" + 
				"  clinical_study_type\n" + 
				"  date_of_iacuc_approval\n" + 
				"  dates_of_conduct\n" + 
				"    principal_investigators{\n" + 
				"        pi_first_name\n" + 
				"        pi_last_name\n" + 
				"        pi_middle_initial\n" + 
				"    }\n" + 
				"    study_arms{\n" + 
				"        arm\n" + 
				"        ctep_treatment_assignment_code\n" + 
				"        cohorts{\n" + 
				"            cohort_description\n" + 
				"            cohort_dose\n" + 
				"        }\n" + 
				"    }\n" + 
				" }\n" + 
				"}"
				+ "\"}");
	
	}
	
	
	
	@Test
	public void testAPISamplesOfCases() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				"samplesByCaseId(case_id: \\\"COTC007B0901\\\")\n" + 
				"{ \n" + 
				"sample_id\n" + 
				"}\n" + 
				"}"
				+ "\"}");
	
	}
	
	
	
	@Test
	public void testAPIFilesOfCases() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				"   fileCountOfCase(case_id: \\\"COTC007B0901\\\")\n" + 
				"}"
				+ "\"}");
	
	}
	
	
	@Test
	public void testAPIAliquotesOfCases() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {\n" + 
				"    aliquotCountOfCase(case_id: \\\"COTC007B0901\\\")\n" + 
				"}"
				+ "\"}");
	
	}
	
	
	
	@Test
	public void testAPICasesDetails() throws Exception {
	
		this.testGraphQLAPI("{\"query\":\" {  case(patient_id:\\\"0220\\\"){\n" + 
				"    patient_id\n" + 
				"    patient_first_name\n" + 
				"    demographic{\n" + 
				"        breed\n" + 
				"        sex\n" + 
				"        patient_age_at_enrollment\n" + 
				"    }\n" + 
				"    cohort{\n" + 
				"        cohort_description\n" + 
				"        cohort_dose\n" + 
				"        study_arm{\n" + 
				"            arm\n" + 
				"            ctep_treatment_assignment_code\n" + 
				"        }\n" + 
				"    }\n" + 
				"    enrollment{\n" + 
				"        patient_subgroup\n" + 
				"        date_of_informed_consent\n" + 
				"    }\n" + 
				"    diagnoses{\n" + 
				"        disease_term\n" + 
				"        stage_of_disease\n" + 
				"        date_of_diagnosis\n" + 
				"        primary_disease_site\n" + 
				"        histological_grade\n" + 
				"        histology_cytopathology\n" + 
				"    }\n" + 
				"    }\n" + 
				"}\n" + 
				"    "
				+ "\"}");
	
	}
	
	
	
	
	
	@Test
	public void testGraphQLEndPointWithValidPayLoad() throws Exception {

		this.mockMvc
				.perform(RestDocumentationRequestBuilders.post("/v1/graphql/").contentType(MediaType.APPLICATION_JSON)
						.content("{\"query\":\"{study{clinical_study_id}}\"}"))
				.andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("data")))
				.andDo(document("{ClassName}/{methodName}"));

	}

	@Test
	public void testGraphQLEndPointWithInValidPayLoad() throws Exception {

		this.mockMvc
				.perform(RestDocumentationRequestBuilders.post("/v1/graphql/").contentType(MediaType.APPLICATION_JSON)
						.content("{\"query\":\"{e{id}}\"}"))
				.andDo(print()).andExpect(status().is4xxClientError()).andDo(document("{ClassName}/{methodName}"));

	}

}
