package com.karpov.blog.controllers;

import com.karpov.blog.models.Article;
import com.karpov.blog.models.Post;
import com.karpov.blog.repo.ArticleRepository;
import com.karpov.blog.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private ArticleRepository articleRepository;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home Page");
		Iterable<Post> posts = postRepository.findAll(Sort.by("timestamp").descending());
		model.addAttribute("posts", posts);
		Article recentArticle = articleRepository.findTopByOrderByTimestampDesc();
		model.addAttribute("recentArticle", recentArticle);
		return "home";
	}
}
