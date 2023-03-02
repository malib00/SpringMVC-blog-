package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
public class PostsController {

	@Autowired
	private PostRepository postRepository;

	@Value("${upload.path}")
	private String uploadPath;

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

		String userPath = uploadPath + "/" + user.getId();
		File uploadDir = new File(userPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}
		String fileName = UUID.randomUUID() + "." + file.getOriginalFilename();
		file.transferTo(new File(userPath + "/" + fileName));
		post.setFilename(fileName);

		postRepository.save(post);
		return "redirect:/";
	}

	@GetMapping("/{id}")
	public String getPost(@PathVariable(value = "id") long postId, Model model) {
		Optional<Post> post = postRepository.findById(postId);
		if (post.isPresent()) {
			Post retrievedPost = post.get();
			model.addAttribute("title", retrievedPost.getTitle());
			model.addAttribute("post", retrievedPost);
			return "post-details";
		} else {
			return "404";
		}
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
	                         @PathVariable(value = "post") Post post,
	                         @RequestParam String title,
	                         @RequestParam String fullText,
	                         @RequestParam("file") MultipartFile file,
	                         Model model) throws IOException {
			Post editedPost = post;
			editedPost.setTitle(title);
			editedPost.setFullText(fullText);
			if (!file.isEmpty()) {
				String oldFileName = editedPost.getFilename();
				String userPath = uploadPath + "/" + user.getId();
				File uploadDir = new File(userPath);
				if (!uploadDir.exists()) {
					uploadDir.mkdir();
				}
				String fileName = UUID.randomUUID() + "." + file.getOriginalFilename();
				file.transferTo(new File(userPath + "/" + fileName));
				editedPost.setFilename(fileName);
				Files.deleteIfExists(new File(userPath + "/" + oldFileName).toPath());
			}
			postRepository.save(editedPost);
			return "redirect:/";

	}

	@PreAuthorize("#post.author.id == principal.id" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{id}/remove")
	public String postDelete(@PathVariable(value = "id") Post post, Model model) throws IOException {
			String userPath = uploadPath + "/" + post.getAuthor().getId();
			Files.deleteIfExists(new File(userPath + "/" + post.getFilename()).toPath());
			postRepository.delete(post);
			return "redirect:/";
	}
}
