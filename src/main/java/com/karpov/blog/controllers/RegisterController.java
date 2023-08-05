package com.karpov.blog.controllers;

import com.karpov.blog.models.User;
import com.karpov.blog.service.RegisterService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@PreAuthorize("isAnonymous() || hasAnyAuthority('MODERATOR','ADMIN')")
public class RegisterController {

	@Autowired
	private RegisterService registerService;

	@GetMapping("/register")
	public String registrationPage(User user, Model model) {
		model.addAttribute("pageTitle", "Registration");
		model.addAttribute("googleCaptchaPublicKey", registerService.getRecaptchaPublicKey());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@Valid User user, BindingResult bindingResult,
	                           @RequestParam(name = "passwordConfirmation", required = false) String passwordConfirmation,
	                           @RequestParam(name = "g-recaptcha-response", required = false) String recaptchaResponse,
	                           Model model) {
		if (registerService.usernameAlreadyTaken(user.getUsername())) {
			bindingResult.addError(new FieldError("user", "username", "Username is already in use. Please choose another one."));
		}
		if (user.getPassword()==null || !user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
			bindingResult.addError(new FieldError("user", "password",
					"Password must contain minimum eight characters, at least one uppercase letter, " +
							"one lowercase letter, one number and one special character (@$!%*#?&)."));
		}
		if (passwordConfirmation == null || !passwordConfirmation.equals(user.getPassword())) {
			bindingResult.addError(new FieldError("user", "password", "Passwords are not the same."));
		}
		if (!registerService.captchaResponseSuccessful(recaptchaResponse)) {
			model.addAttribute("captchaError", "Please complete captcha form.");
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", "Registration");
			model.addAttribute("googleCaptchaPublicKey", registerService.getRecaptchaPublicKey());
			return "register";
		} else {
			registerService.registerUser(user);
			model.addAttribute("infoMessage", "Registration successful. " +
					"Please follow the link with activation code, that we sent to your e-mail. After that you can login.");
			log.info("User registration. (id: {}, username: {})", user.getId(), user.getUsername());
			return "login";
		}
	}

	@GetMapping("activate/{code}")
	public String activateUser(@PathVariable String code, Model model) {
		boolean userIsActivated = registerService.activateUser(code);
		if (userIsActivated) {
			model.addAttribute("activationIsSuccessful", true);
			model.addAttribute("activationStatusMessage", "Your profile is successfully activated! \n You can login now.");
		} else {
			model.addAttribute("activationIsSuccessful", false);
			model.addAttribute("activationStatusMessage", "Activation code not found or profile is already activated");
			log.warn("Unsuccessful user activation. (code: {}", code);
		}
		return "login";
	}
}
