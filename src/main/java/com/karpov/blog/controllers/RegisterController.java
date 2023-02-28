package com.karpov.blog.controllers;

import com.karpov.blog.models.User;
import com.karpov.blog.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/register")
public class RegisterController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping
	public String registrationPage(Model model) {
		model.addAttribute("title", "Registration");
		return "user-register";
	}

	@PostMapping
	public String addUser(@RequestParam String username, @RequestParam String fullName, @RequestParam String password, Model model) {
		User user = new User(username,fullName,password);
		user.setActive(true);
		userRepository.save(user);
		return "redirect:/";
	}
}
