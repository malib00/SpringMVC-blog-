package com.karpov.blog.controllers;

import com.karpov.blog.models.Article;
import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.ArticleRepository;
import com.karpov.blog.service.ImageFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/articles")
public class ArticlesController {

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private ImageFileService imageFileServisce;

	@GetMapping("")
	public String getArticles(Model model) {
		model.addAttribute("title", "Articles");
		Iterable<Article> articles = articleRepository.findAll(Sort.by("timestamp").descending());
		model.addAttribute("articles", articles);
		return "articles";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/add")
	public String addArticle(Model model) {
		model.addAttribute("title", "Add Article");
		return "article-add";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/add")
	public String addArticle(@AuthenticationPrincipal User user,
	                      @RequestParam String title,
	                      @RequestParam String fulltext,
	                      @RequestParam("file") MultipartFile file,
	                      Model model) throws IOException {
		Article article = new Article(title, fulltext, user);
		String fileName = imageFileServisce.save(file, "articles");
		article.setFilename(fileName);
		articleRepository.save(article);
		return "redirect:/articles";
	}

	@GetMapping("/{article}")
	public String getArticle(@PathVariable Article article, Model model) {
		model.addAttribute("title", article.getTitle());
		model.addAttribute("article", article);
		return "article-details";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{article}/edit")
	public String editArticle(@PathVariable Article article, Model model) {
		model.addAttribute("title", "Article Edit: " + article.getTitle());
		model.addAttribute("article", article);
		return "article-edit";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{article}/edit")
	public String articleUpdate(@PathVariable Article article,
	                         @RequestParam String title,
	                         @RequestParam String fulltext,
	                         @RequestParam("file") MultipartFile file,
	                         Model model) throws IOException {
		article.setTitle(title);
		article.setFulltext(fulltext);
		if (!file.isEmpty()) {
			String oldFileName = article.getFilename();
			String newFileName = imageFileServisce.replace(file, "articles", oldFileName);
			article.setFilename(newFileName);
		}
		articleRepository.save(article);
		return "redirect:/";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{article}/remove")
	public String postDelete(@PathVariable Article article, Model model) throws IOException {
		String fileName = article.getFilename();
		imageFileServisce.delete("articles", fileName);
		articleRepository.delete(article);
		return "redirect:/";
	}
}
