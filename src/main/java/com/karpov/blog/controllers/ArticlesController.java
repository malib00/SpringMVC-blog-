package com.karpov.blog.controllers;

import com.karpov.blog.models.Article;
import com.karpov.blog.models.User;
import com.karpov.blog.service.ArticleService;
import com.karpov.blog.service.ImageFileService;
import com.uploadcare.upload.UploadFailureException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/articles")
public class ArticlesController {

	@Autowired
	private ArticleService articleService;

	@Autowired
	private ImageFileService imageFileService;

	@GetMapping("")
	public String getArticles(Model model) {
		model.addAttribute("pageTitle", "Articles");
		Iterable<Article> articles = articleService.getAllArticles();
		model.addAttribute("articles", articles);
		return "article/articles";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/add")
	public String addArticleBlankPage(Article article, Model model) {
		model.addAttribute("pageTitle", "Add Article");
		return "article/article-add";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/add")
	public String addArticle(@AuthenticationPrincipal User authenticatedUser,
	                         @Valid Article article, BindingResult bindingResult,
	                         @RequestParam("file") MultipartFile multipartFile,
	                         Model model) {
		if (multipartFile.isEmpty()) {
			bindingResult.addError(new FieldError("post", "imageFile", "File is empty"));
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", "Add Post");
			return "article/article-add";
		} else {
			try {
				articleService.addNewArticle(article, multipartFile, authenticatedUser);
				log.info("Article (id={}) is created by (id={}, username={}).",
						article.getId(), authenticatedUser.getId(), authenticatedUser.getUsername());
			} catch (IOException e) {
				bindingResult.addError(new FieldError("article", "imageFile", "File upload error"));
				log.error("Error while uploading image to article");
				return "article/article-add";
			} catch (UploadFailureException e) {
				log.error("Error while uploading image to UploadCare service");
				bindingResult.addError(new FieldError("article", "imageFile", "File upload service error"));
				return "article/article-add";
			}
			return "article/article-details";
		}
	}

	@GetMapping("/{article}")
	public String getArticle(@PathVariable Article article, Model model) {
		model.addAttribute("pageTitle", article.getTitle());
		model.addAttribute("article", article);
		return "article/article-details";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{article}/edit")
	public String editArticle(@PathVariable Article article, Model model) {
		model.addAttribute("pagetitle", "Article Edit: " + article.getTitle());
		model.addAttribute("editedArticle", article);
		return "article/article-edit";
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{article}/edit")
	public String articleUpdate(@AuthenticationPrincipal User authenticatedUser,
	                            @PathVariable Article article,
	                            @ModelAttribute(name = "editedArticle") @Valid Article editedArticle, BindingResult bindingResult,
	                            @RequestParam("file") MultipartFile multipartFile,
	                            Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", "Article Edit: " + article.getTitle());
			return "article/article-edit";
		} else {
			try {
				articleService.updateArticle(article, editedArticle, multipartFile);
				log.info("Article (id={}) was edited by (id={}, username={}).",
						article.getId(), authenticatedUser.getId(), authenticatedUser.getUsername());
			} catch (IOException e) {
				bindingResult.addError(new FieldError("article", "imageFile", "File upload error"));
				log.error("Error while uploading image to article");
				return "article/article-edit";
			} catch (UploadFailureException e) {
				log.error("Error while uploading image to UploadCare service");
				bindingResult.addError(new FieldError("article", "imageFile", "File upload service error"));
				return "article/article-edit";
			}
			return "article/article-details";
		}
	}

	@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{article}/remove")
	public String deleteArticle(@AuthenticationPrincipal User authenticatedUser,
	                            @PathVariable Article article,
	                            Model model) {
		articleService.deleteArticle(article);
		log.info("Article (old id={}, authorId={}, authorUsername={}) was deleted by user (id={}, username={}.",
				article.getId(), article.getAuthor().getId(), article.getAuthor().getUsername(),
				authenticatedUser.getId(), authenticatedUser.getUsername());
		return "redirect:/";
	}
}
