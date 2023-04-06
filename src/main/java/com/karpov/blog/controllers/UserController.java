package com.karpov.blog.controllers;

import com.karpov.blog.models.Password;
import com.karpov.blog.models.Post;
import com.karpov.blog.models.Role;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.karpov.blog.repo.UserRepository;
import com.karpov.blog.service.ImageFileService;
import com.karpov.blog.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Collection;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping("/users")
@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
public class UserController {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private ImageFileService imageFileService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@GetMapping
	public String usersListForAdmins(Model model) {
		model.addAttribute("title", "User List");
		model.addAttribute("users", userRepository.findAll(Sort.by("id").ascending()));
		return "user/users-list-for-admins";
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
		model.addAttribute("totalPostsQTY", user.getPosts().size());
		model.addAttribute("totalFollowers", user.getFollowers().size());
		model.addAttribute("totalFollowing", user.getFollowing().size());
		Iterable<Post> last3Posts = postRepository.findFirst3ByAuthor(user, Sort.by("timestamp").descending());
		model.addAttribute("last3Posts", last3Posts);
		return "user/user-profile";
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
	@GetMapping("/{user}/followers")
	public String getUserFollowersList(@PathVariable User user,
	                                   Model model) {
		model.addAttribute("title", user.getUsername() + "'s followers");
		model.addAttribute("users", user.getFollowers());
		return "user/users-list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{user}/following")
	public String getUserFollowingList(@PathVariable User user,
	                                   Model model) {
		model.addAttribute("title", user.getUsername() + "'s following");
		model.addAttribute("users", user.getFollowing());
		return "user/users-list";
	}

	@PreAuthorize("#user.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{user}/edit")
	public String editUser(@PathVariable User user,
	                       Model model) {
		model.addAttribute("title", user.getUsername() + "'s profile edit");
		model.addAttribute("allRoles", Role.values());
		model.addAttribute("uneditedUser", user);
		model.addAttribute("editedUser", user);
		return "user/user-profile-edit";
	}

	@PreAuthorize("#user.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{user}/edit")
	public String updateUser(@AuthenticationPrincipal User authenticatedUser,
	                         @PathVariable User user,
	                         @RequestParam(value = "file") MultipartFile file,
	                         @RequestParam(required = false) Set<Role> roles,
	                         @ModelAttribute(name = "password") @Valid Password password, BindingResult passwordBindingResult,
	                         @ModelAttribute(name = "editedUser") @Valid User editedUser, BindingResult bindingResult,
	                         Model model) throws IOException {
		if (password.getPassword() == null) {
			editedUser.setPassword(user.getPassword());
		} else if (passwordBindingResult.hasErrors()) {
			passwordBindingResult.getAllErrors().stream().forEach(x -> bindingResult.addError(x));
		}
		User userWithSameUsername = userRepository.findByUsername(editedUser.getUsername());
		if (userWithSameUsername != null && userWithSameUsername.getId() != user.getId()) {
			bindingResult.addError(new FieldError("user", "username", "Username is already in use. Please choose another one."));
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("title", user.getUsername() + "'s profile edit");
			model.addAttribute("allRoles", Role.values());
			model.addAttribute("uneditedUser", user);
			model.addAttribute("editedUser", editedUser);
			return "user/user-profile-edit";
		} else {
			editedUser.setId(user.getId());
			if (password.getPassword() != null) {
				editedUser.setPassword(passwordEncoder.encode(password.getPassword()));
			}
			if (!file.isEmpty()) {
				String oldFileName = editedUser.getAvatar();
				String path = String.valueOf(editedUser.getId());
				String newFileName = imageFileService.replace(file, path, oldFileName);
				editedUser.setAvatar(newFileName);
			}
			userRepository.save(editedUser);
			log.info("User (id: {}, username: {}) was edited by user id: {}, username: {}.",
					user.getId(), user.getUsername(),
					authenticatedUser.getId(), authenticatedUser.getUsername());
			return "redirect:/users/{user}";
		}
	}

	@PreAuthorize("permitAll")
	@GetMapping("/{user}/posts")
	public String editPost(@PathVariable User user,
	                       @PageableDefault(sort = {"timestamp"}, size = 3, direction = Sort.Direction.DESC) Pageable pageable,
	                       Model model) {
		model.addAttribute("title", user.getUsername() + "'s posts");
		Page<Post> page = postRepository.findByAuthor(user, pageable.previousOrFirst());
		model.addAttribute("page", page);
		return "user/user-posts";
	}

	@PreAuthorize("#user.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{user}/feed")
	public String getUserFeed(@PathVariable User user,
	                          Model model) throws IOException {
		model.addAttribute("title", user.getUsername() + "'s feed");
		Iterable<Post> posts = postRepository.findByAuthorInOrderByTimestampDesc(user.getFollowing());
		model.addAttribute("posts", posts);
		return "user/user-posts";
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping("/{user}/delete")
	public String deleteUser(@AuthenticationPrincipal User authenticatedUser,
	                         @PathVariable User user,
	                         Model model) {
		imageFileService.deleteUserImages(String.valueOf(user.getId()));
		userRepository.delete(user);
		log.info("User (old id: {}, username: {}) was deleted by user id: {}, username: {}.",
				user.getId(), user.getUsername(),
				authenticatedUser.getId(), authenticatedUser.getUsername());
		model.addAttribute("title", "User List");
		return "redirect:/users";
	}
}
