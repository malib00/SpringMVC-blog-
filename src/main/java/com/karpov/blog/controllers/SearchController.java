package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

	@Autowired
	private PostRepository postRepository;

	@GetMapping("/search")
	public String main(@RequestParam(required = false, defaultValue = "") String filter, Model model) {
		Iterable<Post> foundPosts = postRepository.findByTitleContaining(filter, Sort.by("timestamp").descending());
		model.addAttribute("posts", foundPosts);
		return "posts";
	}
}
