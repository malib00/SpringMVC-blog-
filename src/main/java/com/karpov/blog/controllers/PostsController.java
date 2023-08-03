package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.service.PostService;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/posts")
public class PostsController {

	@Autowired
	private PostService postService;

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/add")
	public String addPostPage(Post post, Model model) {
		model.addAttribute("title", "Add Post");
		return "post/post-add";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/add")
	public String addPost(
			@AuthenticationPrincipal User authenticatedUser,
			@Valid Post post, BindingResult bindingResult,
			@RequestParam("file") MultipartFile multipartFile,
			Model model) {
		if (multipartFile.isEmpty()) {
			bindingResult.addError(new FieldError("post", "imageFile", "File is empty"));
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("title", "Add Post");
			return "post/post-add";
		} else {
			try {
				postService.addNewPost(post, multipartFile, authenticatedUser);
				log.info("Post (id={}) is created by (id={}, username={}).",
						post.getId(), authenticatedUser.getId(), authenticatedUser.getUsername());
			} catch (IOException e) {
				bindingResult.addError(new FieldError("post", "imageFile", "File upload error"));
				log.error("Error while uploading image to post");
				return "post/post-add";
			} catch (UploadFailureException e) {
				log.error("Error while uploading image to UploadCare service");
				bindingResult.addError(new FieldError("post", "imageFile", "File upload service error"));
				return "post/post-add";
			}
			return "post/post-details";
		}
	}

	@GetMapping("/{post}")
	public String getPost(@PathVariable Post post, Model model) {
		model.addAttribute("pageTitle", post.getTitle());
		return "post/post-details";
	}

	@PreAuthorize("#post.author.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{post}/edit")
	public String editPost(@PathVariable Post post, Model model) {
		model.addAttribute("pageTitle", "Post Edit: " + post.getTitle());
		model.addAttribute("editedPost", post);
		return "post/post-edit";
	}

	@ExceptionHandler(TemplateProcessingException.class)
	@PreAuthorize("#post.author.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{post}/edit")
	public String postUpdate(@AuthenticationPrincipal User authenticatedUser,
	                         @PathVariable Post post,
	                         @ModelAttribute(name = "editedPost") @Valid Post editedPost, BindingResult bindingResult,
	                         @RequestParam("file") MultipartFile multipartFile,
	                         Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", "Post Edit: " + post.getTitle());
			return "post/post-edit";
		} else {
			try {
				postService.updatePost(post, editedPost, multipartFile);
				log.info("Post (id={}) is edited by (id={}, username={}).",
						editedPost.getId(), authenticatedUser.getId(), authenticatedUser.getUsername());
			} catch (IOException e) {
				bindingResult.addError(new FieldError("post", "imageFile", "File upload error"));
				log.error("Error while uploading image to post");
				return "post/post-edit";
			} catch (UploadFailureException e) {
				log.error("Error while uploading image to UploadCare service");
				bindingResult.addError(new FieldError("post", "imageFile", "File upload service error"));
				return "post/post-edit";
			}
			return "post/post-details";
		}
	}

	@PreAuthorize("#post.author.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{post}/remove")
	public String postDelete(@AuthenticationPrincipal User authenticatedUser,
	                         @PathVariable Post post,
	                         Model model) {
		postService.deletePost(post);
		log.info("Post (old id={}, authorId={}, authorUsername={}) was deleted by user (id={}, username={}.",
				post.getId(), post.getAuthor().getId(), post.getAuthor().getUsername(),
				authenticatedUser.getId(), authenticatedUser.getUsername());
		return "redirect:/";
	}
}
