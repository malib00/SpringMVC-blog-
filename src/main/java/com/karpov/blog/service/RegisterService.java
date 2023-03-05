package com.karpov.blog.service;

import com.karpov.blog.models.User;
import com.karpov.blog.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RegisterService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MailSenderService mailSender;

	@Value("${hostname}")
	private String hostname;

	public boolean registerUser(User user) {
		User userWithSameUsername = userRepository.findByUsername(user.getUsername());
		if (userWithSameUsername != null) {
			return false;
		}
		user.setEmailActivationCode(UUID.randomUUID().toString());
		userRepository.save(user);

		String welcomeMessage = String.format(
				"Hello, %s! \n" +
						"Welcome to our PhotoBlog!\n" +
						"Please, activate your account by following this link: http://%s/activate/%s",
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
}
