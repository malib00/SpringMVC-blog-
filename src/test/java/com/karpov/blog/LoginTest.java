package com.karpov.blog;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

	@Autowired
	private MockMvc mockMvc;

	@Value("${testUsername}")
	private String testUsername;

	@Value("${testPassword}")
	private String testPassword;

	@Test
	public void correctLoginTest() throws Exception {
		this.mockMvc.perform(formLogin().user(testUsername).password(testPassword))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"));
	}

	@Test
	public void incorrectLoginTest() throws Exception {
		this.mockMvc.perform(formLogin().user("somewrongusername"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"));
	}

	@Test
	public void loginRequiredTest() throws Exception {
		this.mockMvc.perform(get("/posts/add")).andDo(print())
				.andExpect(status().is3xxRedirection());
	}
}
