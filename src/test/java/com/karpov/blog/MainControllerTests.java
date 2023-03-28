package com.karpov.blog;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-users-before.sql", "/create-posts-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-posts-after.sql", "/create-users-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MainControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	static void createImageDirectory() {
		File directory = new File("/E:/blogSpringFilesTest/data");
		if (!directory.exists()) {
			try {
				directory.mkdirs();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void mainPageTest() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Home Page")));
	}

	@WithUserDetails(value = "dante")
	@Test
	public void mainPageForUserTest() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(authenticated())
				.andExpect(xpath("//*[@id='usernameHeader']").string("@dante"));
	}


	@Test
	public void mainPageWithContentTest() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(xpath("//div[@id='postsList']/div").nodeCount(4));
	}

	@WithUserDetails(value = "dante")
	@Test
	public void filterTest() throws Exception {
		this.mockMvc.perform(get("/search").param("filter", "title2")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(authenticated())
				.andExpect(xpath("//div[@id='postsList']/div").nodeCount(2))
				.andExpect(xpath("//div[@id='postsList']/div/div/div/div[@data-id=51]").exists())
				.andExpect(xpath("//div[@id='postsList']/div/div/div/div[@data-id=151]").exists());
	}

	@WithUserDetails(value = "vergil")
	@Test
	public void addPostTest() throws Exception {
		File postTestFile = new File("src/test/resources/postTestFile.jpg");
		FileInputStream fileInputStream = new FileInputStream(postTestFile);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", postTestFile.getName(), "multipart/form-data", fileInputStream);

		MockHttpServletRequestBuilder multipart = multipart("/posts/add")
				.file(mockMultipartFile)
				.param("title", "title5")
				.param("fulltext", "Some full post text.")
				/*.with(csrf())*/;

		this.mockMvc.perform(multipart)
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated());

		this.mockMvc.perform(get("/")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(xpath("//div[@id='postsList']/div").nodeCount(5))
				.andExpect(xpath("//div[@id='postsList']/div/div/div/div[@data-id=951]").exists())
				.andExpect(xpath("//div[@id='postsList']/div/div/div/div[@data-id=951]/div/div/p").string("title5"));
	}

	@AfterAll
	static void cleanImageDirectory() {
		File directory = new File("/E:/blogSpringFilesTest");
		if (directory.exists()) {
			try {
				FileUtils.deleteDirectory(directory);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
