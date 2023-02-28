package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	//@PreAuthorize("hasRole('USER')") //TODO possible restrictions to pages
	//@PreAuthorize("hasAuthority('USER')")
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

	@GetMapping("/{id}/edit")
	public String editPost(@PathVariable(value = "id") long postId, Model model) {
		Optional<Post> post = postRepository.findById(postId);
		if (post.isPresent()) {
			Post retrievedPost = post.get();
			model.addAttribute("title", "Post Edit: " + retrievedPost.getTitle());
			model.addAttribute("post", retrievedPost);
			return "post-edit";
		} else {
			return "404";
		}
	}

	@PostMapping("/{id}/edit")
	public String postUpdate(@AuthenticationPrincipal User user,
	                         @PathVariable(value = "id") long postId,
	                         @RequestParam String title,
	                         @RequestParam String fullText,
	                         @RequestParam("file") MultipartFile file,
	                         Model model) throws IOException {
		Optional<Post> post = postRepository.findById(postId);
		if (post.isPresent()) {
			Post editedPost = post.get();
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
		} else {
			return "404";
		}
	}




	@PostMapping("/{id}/remove")
	public String postDelete(@PathVariable(value = "id") Post post, Model model) throws IOException {
			String userPath = uploadPath + "/" + post.getAuthor().getId();
			Files.deleteIfExists(new File(userPath + "/" + post.getFilename()).toPath());
			postRepository.delete(post);
			return "redirect:/";
	}
}
