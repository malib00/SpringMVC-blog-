package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@Autowired
	private PostRepository postRepository;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home Page");
		Iterable<Post> posts = postRepository.findAll();
		model.addAttribute("posts", posts);
		return "home";
	}


}