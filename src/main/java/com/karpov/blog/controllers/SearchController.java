package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.repo.PostRepository;
import com.karpov.blog.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.regex.Pattern;

@Controller
public class SearchController {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/search")
	public String search(@RequestParam String filter, Model model) {
		model.addAttribute("title", "Search...");

		String trimmedFilter = filter.trim();
		Pattern userPattern = Pattern.compile("@.+");
		Pattern postPattern = Pattern.compile(".+");

		if (userPattern.matcher(trimmedFilter).find()) {
			String userFilter = trimmedFilter.substring(1);
			model.addAttribute("users", userRepository.findByUsernameContainingIgnoreCaseOrderByIdDesc(userFilter));
			return "users-list";
		} else if (postPattern.matcher(trimmedFilter).find()) {
			model.addAttribute("posts", postRepository.findByTitleContainingIgnoreCaseOrderByTimestampDesc(trimmedFilter));
		}
		return "posts";
	}
}
