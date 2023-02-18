package com.karpov.blog.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home Page");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About Page");
		return "about";
	}

	@GetMapping("/news")
	public String news(Model model) {
		model.addAttribute("title", "News Page");
		return "news";
	}

}