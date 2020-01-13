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
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RESTControllerTests {
	

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


	@Test
	public void testPing() throws Exception {

		this.mockMvc.perform(RestDocumentationRequestBuilders.get("/ping"))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("pong")))
				.andDo(document("{ClassName}/{methodName}"));

	}
	
//	@Test
//	public void testAPIPrograms() throws Exception {
//
//		this.mockMvc.perform(RestDocumentationRequestBuilders.get("/v1/rest/programs"))
//				.andDo(print()).andExpect(status().isOk())
//				.andExpect(content().string(containsString("data")))
//				.andDo(document("{ClassName}/{methodName}"));
//
//	}
//	
//	@Test
//	public void testAPIStudies() throws Exception {
//
//		this.mockMvc.perform(RestDocumentationRequestBuilders.get("/v1/rest/studies"))
//				.andDo(print()).andExpect(status().isOk())
//				.andExpect(content().string(containsString("data")))
//				.andDo(document("{ClassName}/{methodName}"));
//
//	}
//	
//	@Test
//	public void testAPICases() throws Exception {
//
//		this.mockMvc.perform(RestDocumentationRequestBuilders.get("/v1/rest/cases"))
//				.andDo(print()).andExpect(status().isOk())
//				.andExpect(content().string(containsString("data")))
//				.andDo(document("{ClassName}/{methodName}"));
//
//	}
//	
}
