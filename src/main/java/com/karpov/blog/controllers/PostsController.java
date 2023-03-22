package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.karpov.blog.service.ImageFileServisce;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/posts")
public class PostsController {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private ImageFileServisce imageFileServisce;

	@GetMapping("/add")
	public String addPostPage(Model model) {
		model.addAttribute("title", "Add Post");
		return "post-add";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/add")
	public String addPost(
			@AuthenticationPrincipal User user,
			@RequestParam String title,
			@RequestParam String fullText,
			@RequestParam("file") MultipartFile file,
			Model model) throws IOException {
		Post post = new Post(title, fullText, user);
		String path = String.valueOf(user.getId());
		String fileName = imageFileServisce.save(file, path);
		post.setFilename(fileName);
		postRepository.save(post);
		return "redirect:/";
	}

	@GetMapping("/{post}")
	public String getPost(@PathVariable Post post, Model model) {
		model.addAttribute("title", post.getTitle());
		model.addAttribute("post", post);
		return "post-details";
	}

	@PreAuthorize("#post.author.id == principal.id" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{post}/edit")
	public String editPost(@PathVariable Post post, Model model) {
		model.addAttribute("title", "Post Edit: " + post.getTitle());
		model.addAttribute("post", post);
		return "post-edit";
	}

	@PreAuthorize("#post.author.id == principal.id" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{post}/edit")
	public String postUpdate(@AuthenticationPrincipal User user,  //TODO looks like bug, who is user, who is authenticated
	                         @PathVariable Post post,
	                         @RequestParam String title,
	                         @RequestParam String fullText,
	                         @RequestParam("file") MultipartFile file,
	                         Model model) throws IOException {
		post.setTitle(title);
		post.setFullText(fullText);
		if (!file.isEmpty()) {
			String oldFileName = post.getFilename();
			String path = String.valueOf(user.getId());
			String newFileName = imageFileServisce.replace(file, path, oldFileName);
			post.setFilename(newFileName);
		}
		postRepository.save(post);
		return "redirect:/";
	}

	@PreAuthorize("#post.author.id == principal.id" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{post}/remove")
	public String postDelete(@PathVariable Post post, Model model) throws IOException {
		String path = String.valueOf(post.getAuthor().getId());
		String fileName = post.getFilename();
		imageFileServisce.delete(path, fileName);
		postRepository.delete(post);
		return "redirect:/";
	}
}
