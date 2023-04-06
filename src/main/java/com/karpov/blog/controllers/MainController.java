package com.karpov.blog.controllers;

import com.karpov.blog.models.Article;
import com.karpov.blog.models.Post;
import com.karpov.blog.repo.ArticleRepository;
import com.karpov.blog.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
	public String home(Model model,
	                   @PageableDefault(sort = {"id"}, size = 9, direction = Sort.Direction.DESC) Pageable pageable) {
		model.addAttribute("title", "Home Page");
		Page<Post> page = postRepository.findAll(pageable.previousOrFirst());
		model.addAttribute("page", page);
		model.addAttribute("url", "/");
		Article recentArticle = articleRepository.findTopByOrderByTimestampDesc();
		model.addAttribute("recentArticle", recentArticle);
		return "home";
	}
}
