package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.Role;
import com.karpov.blog.models.User;
import com.karpov.blog.service.UserService;
import com.uploadcare.upload.UploadFailureException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import java.util.Set;

@Slf4j
@Controller
@RequestMapping("/users")
@PreAuthorize("hasAnyAuthority('MODERATOR','ADMIN')")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping
	public String usersListForAdmins(Model model) {
		model.addAttribute("pageTitle", "User List");
		model.addAttribute("users", userService.getAllUsers());
		return "user/users-list-for-admins";
	}

	@PreAuthorize("permitAll")
	@GetMapping("/{user}")
	public String getUser(@AuthenticationPrincipal User authenticatedUser,
	                      @PathVariable User user,
	                      Model model) {
		model.addAttribute("pageTitle", user.getUsername() + "'s profile");
		model.addAttribute("user", user);
		model.addAttribute("itsMyPage", authenticatedUser.equals(user));
		model.addAttribute("isFollowingThisUser", user.getFollowers().contains(authenticatedUser));
		model.addAttribute("totalPostsQTY", user.getPosts().size());
		model.addAttribute("totalFollowers", user.getFollowers().size());
		model.addAttribute("totalFollowing", user.getFollowing().size());
		model.addAttribute("last3Posts", userService.get3RecentPosts(user));
		return "user/user-profile";
	}

	@PreAuthorize("#user.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{user}/edit")
	public String editUser(@PathVariable User user,
	                       Model model) {
		model.addAttribute("pageTitle", user.getUsername() + "'s profile edit");
		model.addAttribute("allRoles", Role.values());
		model.addAttribute("editedUser", user);
		return "user/user-profile-edit";
	}

	@PreAuthorize("#user.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@PostMapping("/{user}/edit")
	public String updateUser(@AuthenticationPrincipal User authenticatedUser,
	                         @PathVariable User user,
	                         @RequestParam(value = "file") MultipartFile multipartFile,
	                         @RequestParam(required = false) Set<Role> roles,
	                         @ModelAttribute(name = "editedUser") @Valid User editedUser, BindingResult bindingResult,
	                         Model model) {
		if (editedUser.getPassword()!=null && !editedUser.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
			bindingResult.addError(new FieldError("user", "password",
					"Password must contain minimum eight characters, at least one uppercase letter, " +
							"one lowercase letter, one number and one special character (@$!%*#?&)."));
		}
		if (!user.getUsername().equalsIgnoreCase(editedUser.getUsername()) && userService.sameUsernameFound(editedUser.getUsername())) {
			bindingResult.addError(new FieldError("user", "username", "Username is already in use. Please choose another one."));
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", user.getUsername() + "'s profile edit");
			model.addAttribute("allRoles", Role.values());
			model.addAttribute("editedUser", editedUser);
			return "user/user-profile-edit";
		} else {
			try {
				userService.updateUser(user, editedUser, authenticatedUser, multipartFile);
				log.info("User (id={}) was edited by (id={}, username={}).",
						user.getId(), authenticatedUser.getId(), authenticatedUser.getUsername());
			} catch (IOException e) {
				bindingResult.addError(new FieldError("user", "avatarImage", "AvatarImage upload error"));
				log.error("Error while uploading avatarImage to profile");
				return "user/user-profile-edit";
			} catch (UploadFailureException e) {
				log.error("Error while uploading avatarImage to UploadCare service");
				bindingResult.addError(new FieldError("user", "avatarImage", "AvatarImage upload service error"));
				return "user/user-profile-edit";
			}
			return "redirect:/users/{user}";
		}
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
		model.addAttribute("pageTitle", user.getUsername() + "'s followers");
		model.addAttribute("users", user.getFollowers());
		return "user/users-list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{user}/following")
	public String getUserFollowingList(@PathVariable User user,
	                                   Model model) {
		model.addAttribute("pageTitle", user.getUsername() + "'s following");
		model.addAttribute("users", user.getFollowing());
		return "user/users-list";
	}

	@PreAuthorize("permitAll")
	@GetMapping("/{user}/posts")
	public String editPost(@PathVariable User user,
	                       @PageableDefault(sort = {"timestamp"}, size = 9, direction = Sort.Direction.DESC) Pageable pageable,
	                       Model model) {
		model.addAttribute("pageTitle", user.getUsername() + "'s posts");
		Page<Post> page = userService.getUserPostsPageable(user, pageable.previousOrFirst());
		model.addAttribute("page", page);
		return "user/user-posts";
	}

	@PreAuthorize("#user.id == principal.id || hasAnyAuthority('MODERATOR','ADMIN')")
	@GetMapping("/{user}/feed")
	public String getUserFeed(@PathVariable User user,
	                          @PageableDefault(sort = {"timestamp"}, size = 9, direction = Sort.Direction.DESC) Pageable pageable,
	                          Model model) {
		model.addAttribute("pageTitle", user.getUsername() + "'s feed");
		Page<Post> page = userService.getUserFeedPageable(user.getFollowing(), pageable.previousOrFirst());
		model.addAttribute("page", page);
		return "user/user-posts";
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping("/{user}/delete")
	public String deleteUser(@AuthenticationPrincipal User authenticatedUser,
	                         @PathVariable User user,
	                         Model model) {
		userService.deleteUser(user);
		log.info("User (oldId={}, username={}) was deleted by (id={}, username={}.",
				user.getId(), user.getUsername(),
				authenticatedUser.getId(), authenticatedUser.getUsername());
		model.addAttribute("pageTitle", "User List");
		return "redirect:/users";
	}
}
