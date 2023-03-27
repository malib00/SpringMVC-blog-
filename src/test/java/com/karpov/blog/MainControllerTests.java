package com.karpov.blog;

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
import java.nio.file.Files;

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

	@Test
	public void MainPageTest() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Home Page")));
	}

	@WithUserDetails(value = "dante")
	@Test
	public void MainPageForUserTest() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(authenticated())
				.andExpect(xpath("//*[@id='usernameHeader']").string("@dante"));
	}


	@Test
	public void MainPageWithContentTest() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(xpath("//div[@id='postsList']/div").nodeCount(4));
	}

	@WithUserDetails(value = "dante")
	@Test
	public void FilterTest() throws Exception {
		this.mockMvc.perform(get("/search").param("filter","title2")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(authenticated())
				.andExpect(xpath("//div[@id='postsList']/div").nodeCount(2))
				.andExpect(xpath("//div[@id='postsList']/div/div/div/div[@data-id=51]").exists())
				.andExpect(xpath("//div[@id='postsList']/div/div/div/div[@data-id=151]").exists());
	}
}
