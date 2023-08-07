package com.karpov.blog.service;

import com.karpov.blog.models.Role;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.UserRepository;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;


@SpringBootTest
class RegisterServiceTest {

	@Autowired
	private RegisterService registerService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@MockBean
	private MailSenderService mailSenderService;

	@Test
	void registerUserTest() {
		User user = new User();
		user.setEmail("test@mail321.com");

		boolean isCreatedUser = registerService.registerUser(user);

		Assertions.assertTrue(isCreatedUser);
		Assertions.assertFalse(user.isActive());
		Assertions.assertNotNull(user.getEmailActivationCode());
		Assertions.assertTrue(CoreMatchers.is(user.getRoles()).matches(Collections.singleton(Role.USER)));
		Mockito.verify(userRepository, Mockito.times(1)).save(user);
		Mockito.verify(mailSenderService, Mockito.times(1))
				.sendActivationCode(
						ArgumentMatchers.eq(user.getEmail()),
						ArgumentMatchers.eq("PhotoBlog: Activate your account"),
						ArgumentMatchers.contains("Welcome to our PhotoBlog!")
				);

	}

	@Test
	void registerUserFailTest() {
		User user = new User();
		user.setUsername("apollon");

		Mockito.doReturn(user)
				.when(userRepository)
				.findByUsernameIgnoreCase("apollon");

		boolean isCreatedUser = registerService.registerUser(user);

		Assertions.assertFalse(isCreatedUser);
		Mockito.verify(userRepository, Mockito.times(0)).save(ArgumentMatchers.any(User.class));
		Mockito.verify(mailSenderService, Mockito.times(0))
				.sendActivationCode(
						ArgumentMatchers.anyString(),
						ArgumentMatchers.anyString(),
						ArgumentMatchers.anyString()
				);
	}

	@Test
	void activateUserTest() {
		User user = new User();
		user.setEmailActivationCode("someActivationCode");

		Mockito.doReturn(user)
				.when(userRepository)
				.findByEmailActivationCode("someActivationCode");

		Assertions.assertFalse(user.isActive());

		boolean userIsActivatedNow = registerService.activateUser("someActivationCode");

		Assertions.assertTrue(user.isActive());
		Assertions.assertNull(user.getEmailActivationCode());
		Mockito.verify(userRepository, Mockito.times(1)).save(user);
	}

	@Test
	void activateUserFailTest() {
		boolean userIsActivatedNow = registerService.activateUser("someActivationCode");

		Assertions.assertFalse(userIsActivatedNow);
		Mockito.verify(userRepository, Mockito.times(0)).save(ArgumentMatchers.any(User.class));
	}
}