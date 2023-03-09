package com.karpov.blog.controllers;

import com.karpov.blog.models.User;
import com.karpov.blog.service.RegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import java.util.Map;

@Controller
@PreAuthorize("isAnonymous()" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
public class RegisterController {

	@Autowired
	private RegisterService registerService;

	@GetMapping("/register")
	public String registrationPage(User user, Model model) {
		model.addAttribute("title", "Registration");
		return "user-register";
	}

	@PostMapping("/register")
	public String registerUser(@RequestParam("password2") String passwordConfirm,
	                           @Valid User user,
	                           BindingResult bindingResult,
	                           Model model) {


		boolean password2Empty = StringUtils.isEmpty(passwordConfirm);
		if (password2Empty) {
			model.addAttribute("password2error", "Password conformation should NOT be empty");
		}

		if (!user.getPassword().equals(passwordConfirm)) {
			model.addAttribute("passwordsEqualsError", "Passwords are not the same.");
		}
		if (password2Empty || bindingResult.hasErrors()) {
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
			return "redirect:/login";
		} else {
			return "redirect:/error"; //TODO replace with message error, not page reload
		}
	}
}
