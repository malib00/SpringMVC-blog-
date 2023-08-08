package com.karpov.blog.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HealthCheckController {

	@GetMapping("/health")
	public String health() {
		return "health";
	}
}
