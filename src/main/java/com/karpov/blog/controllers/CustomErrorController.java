package com.karpov.blog.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

	@GetMapping("/error")
	public String errorPage(Model model, HttpServletRequest request) {

		Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (statusCode == null) {
			model.addAttribute("title", "Error");
			return "error/error-default";
		} else {
			if (Integer.valueOf(statusCode.toString()) == HttpStatus.NOT_FOUND.value()) {
				model.addAttribute("title", "Page is not found.");
				return "error/error-not-found";
			}
			if (Integer.valueOf(statusCode.toString()) == HttpStatus.FORBIDDEN.value()) {
				model.addAttribute("title", "Access denied.");
				return "error/error-access-denied";
			}
			if (Integer.valueOf(statusCode.toString()) == HttpStatus.METHOD_NOT_ALLOWED.value()) {
				model.addAttribute("title", "Access denied.");
				return "error/error-access-denied";
			}
			if (Integer.valueOf(statusCode.toString()) == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				model.addAttribute("title", "Internal Server Error.");
				return "error/error-internal-server-error";
			}
		}
		model.addAttribute("title", "Error");
		return "error/error-default";
	}
}
