package com.karpov.blog.controllers;

import com.karpov.blog.dto.CaptchaResponseDto;
import com.karpov.blog.models.User;
import com.karpov.blog.service.RegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.util.Collections;

@Controller
@PreAuthorize("isAnonymous()" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
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
		return "user-register";
	}

	@PostMapping("/register")
	public String registerUser(@RequestParam("password2") String passwordConfirm,
							   @RequestParam("g-recaptcha-response") String recaptchaResponse,
	                           @Valid User user,
	                           BindingResult bindingResult,
	                           Model model) {
		String url = String.format(RECAPTCHA_URL, recaptchaSecret, recaptchaResponse);
		CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
		if (!response.isSuccess()) {
			model.addAttribute("captchaError","Please complete captcha form");
		}

		boolean password2Empty = StringUtils.isEmpty(passwordConfirm);
		if (password2Empty) {
			model.addAttribute("password2error", "Password conformation should NOT be empty");
		}

		if (!user.getPassword().equals(passwordConfirm)) {
			model.addAttribute("passwordsEqualsError", "Passwords are not the same.");
		}
		if (password2Empty || bindingResult.hasErrors() || !response.isSuccess()) {
			return "user-register";
		} else {
			registerService.registerUser(user);
			return "redirect:/";
		}
	}

	@GetMapping("activate/{code}")
	public String activateUser(@PathVariable String code, Model model) {
		boolean userIsActivated = registerService.activateUser(code);
		if (userIsActivated) {
			model.addAttribute("activationMessage", "Your profile is successfully activated!");
		} else {
			model.addAttribute("activationMessage", "Activation code not found or profile is already activated");
		}
		return "redirect:/login";
	}
}
