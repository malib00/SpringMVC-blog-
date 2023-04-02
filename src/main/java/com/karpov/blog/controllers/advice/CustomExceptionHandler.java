package com.karpov.blog.controllers.advice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler
	public String handleIOException(IOException e, Model model) {
		//TODO log slf4j
		model.addAttribute("title", "Internal Server Error occured during saving an image.");
		return "error/error-internal-server-error";
	}
}
