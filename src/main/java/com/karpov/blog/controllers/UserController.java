package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.Role;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.karpov.blog.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${upload.path}")
	private String uploadPath;

	@GetMapping
	public String usersList(Model model) {
		model.addAttribute("title", "User List");
		model.addAttribute("users", userRepository.findAll());
		return "users-list";
	}


	@PreAuthorize("permitAll")
	@GetMapping("/{user}")
	public String getUser(@PathVariable User user, Model model) {
		model.addAttribute("title", user.getUsername() + "'s profile");
		model.addAttribute("user", user);
		Iterable<Post> posts = postRepository.findByAuthor(user, Sort.by("timestamp").descending());
		int totalPostsQTY = ((Collection<Post>) posts).size();
		model.addAttribute("totalPostsQTY", totalPostsQTY);
		Iterable<Post> last3Posts = postRepository.findFirst3ByAuthor(user, Sort.by("timestamp").descending());
		model.addAttribute("last3Posts", last3Posts);
		return "user-profile";
	}

	@GetMapping("/{user}/edit")
	public String editUser(@PathVariable User user, Model model) {
		model.addAttribute("title", user.getUsername() + "'s profile edit");
		model.addAttribute("user", user);
		model.addAttribute("allRoles", Role.values());
		return "user-edit";
	}

	@PostMapping("/{user}/edit")
	public String updateUser(@PathVariable User user,
	                         @RequestParam String username,
	                         @RequestParam String password,
	                         @RequestParam String about,
	                         @RequestParam("file") MultipartFile file,
	                         @RequestParam(required = false) Set<Role> roles, Model model) throws IOException {
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setAbout(about);
		//user.setPassword(passwordEncoder.encode(password));
		user.setRoles(roles);
		if (!file.isEmpty()) {
			String oldAvatar = user.getAvatar();
			String userPath = uploadPath + "/" + user.getId();
			File uploadDir = new File(userPath);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}
			String fileName = UUID.randomUUID() + "." + file.getOriginalFilename();
			file.transferTo(new File(userPath + "/" + fileName));
			user.setAvatar(fileName);
			Files.deleteIfExists(new File(userPath + "/" + oldAvatar).toPath());
		}
		userRepository.save(user);
		return "redirect:/users/{user}";
	}

	@PreAuthorize("permitAll")
	@GetMapping("/{user}/posts")
	public String editPost(@PathVariable User user, Model model) {
		model.addAttribute("title", user.getUsername() + "'s posts");
		Iterable<Post> posts = postRepository.findByAuthor(user, Sort.by("timestamp").descending());
		model.addAttribute("posts", posts);
		return "user-posts";
	}

	@PostMapping("/{user}/delete")
	public String deleteUser(@PathVariable User user, Model model) {
		userRepository.delete(user);
		return "redirect:/users";
	}
}
