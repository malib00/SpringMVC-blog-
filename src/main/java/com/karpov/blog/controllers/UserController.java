package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/users")
//@PreAuthorize("hasAuthority('USER')") //TODO change to ADMIN
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping
	public String usersList(Model model) {
		model.addAttribute("title", "User List");
		model.addAttribute("users", userRepository.findAll());
		return "users-list";
	}

	@GetMapping("/{user}")
	public String userProfile(@PathVariable User user, Model model) {
		model.addAttribute("title", user.getUsername() + "'s profile");
		model.addAttribute("user", user);
		return "user-profile";
	}

	@GetMapping("/{user}/edit")
	public String editPost(@PathVariable User user, Model model) {
//		Optional<User> editedUser = userRepository.findById(user.getId());
		model.addAttribute("title", user.getUsername() + "'s profile edit");
		model.addAttribute("user", user);
		return "user-edit";
	}

	@PostMapping("/{user}/edit")
	public String postUpdate(@PathVariable User user, @RequestParam String username, @RequestParam String password, Model model) {
		user.setUsername(username);
		user.setPassword(password);
		userRepository.save(user);
		return "redirect:/users/{user}";
	}

	@PostMapping("/{user}/delete")
	public String deleteUser(@PathVariable User user, Model model) {
		userRepository.delete(user);
		return "redirect:/users";
	}
}
