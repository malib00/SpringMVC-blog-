package com.karpov.blog.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {
	@ExceptionHandler
	public String handleIOException(IOException e, Model model) {
		log.error("Error while saving/deleting and image", e);
		model.addAttribute("pageTitle", "Internal Server Error occured during saving an image.");
		return "error/error-internal-server-error";
	}
}
