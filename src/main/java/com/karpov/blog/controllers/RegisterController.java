package com.karpov.blog.controllers;

import com.karpov.blog.models.User;
import com.karpov.blog.repo.UserRepository;
import com.karpov.blog.service.RegisterService;
import com.karpov.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("isAnonymous()" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
public class RegisterController {

/*	@Autowired
	private UserRepository userRepository;*/

	@Autowired
	private RegisterService registerService;

	@GetMapping("/register")
	public String registrationPage(Model model) {
		model.addAttribute("title", "Registration");
		return "user-register";
	}

	@PostMapping("/register")
	public String registerUser(User user, Model model) {
		if (registerService.registerUser(user)) {
			return "redirect:/";
		} else {
			return "redirect:/register"; //TODO replace with message error, not page reload
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
