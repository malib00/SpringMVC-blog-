package com.karpov.blog.controllers;

import com.karpov.blog.dto.CaptchaResponseDto;
import com.karpov.blog.models.Password;
import com.karpov.blog.models.User;
import com.karpov.blog.service.RegisterService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Controller
@PreAuthorize("isAnonymous() || hasAnyAuthority('MODERATOR','ADMIN')")
public class RegisterController {

	private final static String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

	@Autowired
	private RegisterService registerService;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${recaptcha.secret}")
	private String recaptchaSecret;

	@GetMapping("/register")
	public String registrationPage(User user, Model model) {
		model.addAttribute("title", "Registration");
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute(name = "password") @Valid Password password, BindingResult passwordBindingResult,
	                           @RequestParam("password2") String passwordConfirm,
	                           @RequestParam("g-recaptcha-response") String recaptchaResponse,
	                           @Valid User user,
	                           BindingResult bindingResult,
	                           Model model) {
		String url = String.format(RECAPTCHA_URL, recaptchaSecret, recaptchaResponse);
		CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
		if (!response.isSuccess()) {
			model.addAttribute("captchaError", "Please complete captcha form");
			log.info("Captcha error");
		}
		if (password.getPassword() == null) {
			bindingResult.addError(new FieldError("user", "password", "Password should not be empty!"));
		}
		if (registerService.sameUsernameFound(user.getUsername())) {
			bindingResult.addError(new FieldError("user", "username", "Username is already in use. Please choose another one."));
		}
		if (passwordBindingResult.hasErrors()) {
			passwordBindingResult.getAllErrors().stream().forEach(x -> bindingResult.addError(x));
		}

		if (passwordConfirm == null) {
			model.addAttribute("password2error", "Password conformation should NOT be empty");
		}

		if (!password.getPassword().equals(passwordConfirm)) {
			model.addAttribute("passwordsEqualsError", "Passwords are not the same.");
		}
		if (passwordConfirm == null || bindingResult.hasErrors() || !response.isSuccess()) {
			model.addAttribute("title", "Registration");
			return "register";
		} else {
			registerService.registerUser(user);
			log.info("User created. (id: {}, username: {})", user.getId(), user.getUsername());
			return "redirect:/";
		}
	}

	@GetMapping("activate/{code}")
	public String activateUser(@PathVariable String code, Model model) {
		boolean userIsActivated = registerService.activateUser(code);
		if (userIsActivated) {
			model.addAttribute("activationIsSuccessful", true);
			model.addAttribute("activationStatusMessage", "Your profile is successfully activated! \n You can login now.");
			log.info("User activation. (code: {}", code);
		} else {
			model.addAttribute("activationIsSuccessful", false);
			model.addAttribute("activationStatusMessage", "Activation code not found or profile is already activated");
			log.warn("Unsuccessful user activation. (code: {}", code);
		}
		return "login";
	}
}
