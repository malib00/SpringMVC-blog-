package com.karpov.blog.controllers;

import com.karpov.blog.models.Article;
import com.karpov.blog.service.ArticleService;
import com.karpov.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@Autowired
	private PostService postService;

	@Autowired
	private ArticleService articleService;

	@GetMapping("/")
	public String home(Model model,
	                   @PageableDefault(sort = {"timestamp"}, size = 9, direction = Sort.Direction.DESC) Pageable pageable) {
		model.addAttribute("pageTitle", "Home Page");
		Page page = postService.getAllPostsPageable(pageable);
		model.addAttribute("page", page);
		Article recentArticle = articleService.getRecentArticle();
		model.addAttribute("recentArticle", recentArticle);
		return "home";
	}
}
