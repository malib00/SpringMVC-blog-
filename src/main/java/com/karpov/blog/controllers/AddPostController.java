package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddPostController {

	@GetMapping("/posts/add")
	public String addPost(Model model) {
		model.addAttribute("title", "Add Post Page");
		return "post-add";
	}
}