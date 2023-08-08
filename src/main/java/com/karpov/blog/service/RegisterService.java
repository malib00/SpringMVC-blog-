package com.karpov.blog.service;

import com.karpov.blog.dto.CaptchaResponseDto;
import com.karpov.blog.models.Role;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class RegisterService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MailSenderService mailSender;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${server.hostname}")
	private String hostname;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${google.recaptcha.publickey}")
	private String recaptchaPublicKey;
	@Value("${google.recaptcha.secretkey}")
	private String recaptchaSecretKey;
	private final static String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

	public boolean registerUser(User user) {
		User userWithSameUsername = userRepository.findByUsernameIgnoreCase(user.getUsername());
		if (userWithSameUsername != null) {
			return false;
		}
		user.setEmailActivationCode(UUID.randomUUID().toString());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRoles(Collections.singleton(Role.USER));
		user.setTimestamp(Instant.now());
		userRepository.save(user);

		String welcomeMessage = String.format(
				"Hello, %s! \n" +
				"Welcome to our PhotoBlog!\n" +
				"Please, activate your account by following this link: %s/activate/%s",
				user.getUsername(),
				hostname,
				user.getEmailActivationCode()
		);

		mailSender.sendActivationCode(user.getEmail(), "PhotoBlog: Activate your account", welcomeMessage);
		return true;
	}

	public boolean activateUser(String code) {
		User user = userRepository.findByEmailActivationCode(code);
		if (user != null) {
			user.setEmailActivationCode(null);
			user.setActive(true);
			userRepository.save(user);
			return true;
		} else {
			return false;
		}
	}

	public boolean usernameAlreadyTaken(String username) {
		User userWithSameUsername = userRepository.findByUsernameIgnoreCase(username);
		return userWithSameUsername != null;
	}

	public boolean captchaResponseSuccessful(String recaptchaResponse) {
		String url = String.format(RECAPTCHA_URL, recaptchaSecretKey, recaptchaResponse);
		CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
		try {
			return response.isSuccess();
		} catch (NullPointerException e) {
			log.error("Google captcha service error! Response: {}", response);
			return false;
		}
	}

	public String getRecaptchaPublicKey() {
		return recaptchaPublicKey;
	}
}
