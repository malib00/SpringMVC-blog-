package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.Role;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.karpov.blog.repo.UserRepository;
import com.karpov.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${upload.path}")
	private String uploadPath;

	@GetMapping
	public String usersListForAdmins(Model model) {
		model.addAttribute("title", "User List");
		model.addAttribute("users", userRepository.findAll(Sort.by("id").ascending()));
		return "users-list-for-admins";
	}

	@PreAuthorize("permitAll")
	@GetMapping("/{user}")
	public String getUser(@AuthenticationPrincipal User authenticatedUser,
	                      @PathVariable User user,
	                      Model model) {
		model.addAttribute("title", user.getUsername() + "'s profile");
		model.addAttribute("user", user);
		model.addAttribute("itsMyPage", authenticatedUser.equals(user));
		model.addAttribute("isFollowingThisUser", user.getFollowers().contains(authenticatedUser));
		Iterable<Post> posts = postRepository.findByAuthor(user, Sort.by("timestamp").descending());
		model.addAttribute("totalPostsQTY", ((Collection<Post>) posts).size());
		model.addAttribute("totalFollowers", user.getFollowers().size());
		model.addAttribute("totalFollowing", user.getFollowing().size());
		Iterable<Post> last3Posts = postRepository.findFirst3ByAuthor(user, Sort.by("timestamp").descending());
		model.addAttribute("last3Posts", last3Posts);
		return "user-profile";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{user}/follow")
	public String followUser(@AuthenticationPrincipal User authenticatedUser,
	                         @PathVariable User user,
	                         Model model) {
		userService.follow(authenticatedUser, user);
		return "redirect:/users/{user}";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{user}/unfollow")
	public String unfollowUser(@AuthenticationPrincipal User authenticatedUser,
	                           @PathVariable User user,
	                           Model model) {
		userService.unfollow(authenticatedUser, user);
		return "redirect:/users/{user}";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{user}/{followType}")
	public String getUserFollowList(@PathVariable User user,
	                                @PathVariable String followType,
	                                Model model) {
		if (followType.equals("followers")) {
			model.addAttribute("title", user.getUsername() + "'s followers");
			model.addAttribute("users", user.getFollowers());
			return "users-list";
		} else if (followType.equals("following")) {
			model.addAttribute("title", user.getUsername() + "'s following");
			model.addAttribute("users", user.getFollowing());
			return "users-list";
		} else {
			return "error";
		}
	}

	@PreAuthorize("#user.id == principal.id" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{user}/edit")
	public String editUser(@PathVariable User user,
	                       Model model) {
		model.addAttribute("title", user.getUsername() + "'s profile edit");
		model.addAttribute("user", user);
		model.addAttribute("allRoles", Role.values());
		return "user-edit";
	}

	@PreAuthorize("#user.id == principal.id" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{user}/edit")
	public String updateUser(@RequestParam("file") MultipartFile file,
	                         @RequestParam(required = false) Set<Role> roles,
	                         @Valid User user,
	                         BindingResult bindingResult,
	                         Model model) throws IOException {
		if (bindingResult.hasErrors()) {
			return "user-edit";
		} else {

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
	}

	@PreAuthorize("permitAll")
	@GetMapping("/{user}/posts")
	public String editPost(@PathVariable User user,
	                       Model model) {
		model.addAttribute("title", user.getUsername() + "'s posts");
		Iterable<Post> posts = postRepository.findByAuthor(user, Sort.by("timestamp").descending());
		model.addAttribute("posts", posts);
		return "user-posts";
	}

	@PreAuthorize("#user.id == principal.id" + "|| hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{user}/feed")
	public String getUserFeed(@PathVariable User user,
	                          Model model) throws IOException {
		model.addAttribute("title", user.getUsername() + "'s feed");
		Iterable<Post> posts = postRepository.findByAuthorInOrderByTimestampDesc(user.getFollowing());
		model.addAttribute("posts", posts);
		return "user-posts";
	}

	@PostMapping("/{user}/delete")
	public String deleteUser(@PathVariable User user,
	                         Model model) {
		userRepository.delete(user);
		return "redirect:/users";
	}
}
