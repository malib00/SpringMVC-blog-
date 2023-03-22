package com.karpov.blog.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	@PreAuthorize("isAnonymous()")
	@GetMapping("/login")
	public String loginPage(Model model) {
		model.addAttribute("title", "Login");
		return "login";
	}
}
