package com.karpov.blog.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
	@GetMapping("/login")
	public String loginPage(Model model) {
		model.addAttribute("title", "Log In");
		return "login";
	}
/*
	@PostMapping("/login")
	public String login(Model model) {
		model.addAttribute("title", "Log In");
		return "login";
	}*/
}
