package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.service.PostService;
import com.karpov.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.regex.Pattern;

@Controller
public class SearchController {

	@Autowired
	private UserService userService;

	@Autowired
	private PostService postService;

	@GetMapping("/search")
	public String search(@RequestParam String filter,
	                     @PageableDefault(sort = {"timestamp"}, size = 9, direction = Sort.Direction.DESC) Pageable pageable,
	                     Model model) {
		model.addAttribute("pageTitle", "Search: " + filter);

		String trimmedFilter = filter.trim();
		Pattern userPattern = Pattern.compile("@.+");
		Pattern postPattern = Pattern.compile(".+");

		if (userPattern.matcher(trimmedFilter).find()) {
			String trimmedUserFilter = trimmedFilter.substring(1);
			Page<User> page = userService.findByUsernameCaseInsensitivePageable(trimmedUserFilter, pageable.previousOrFirst());
			model.addAttribute("pageTitle", "Search: " + trimmedFilter);
			model.addAttribute("page", page);
			model.addAttribute("filter", trimmedFilter);
			return "user/users-list";
		} else if (postPattern.matcher(trimmedFilter).find()) {
			Page<Post> page = postService.findByTitleCaseInsensitivePageable(trimmedFilter, pageable.previousOrFirst());
			model.addAttribute("pageTitle", "Search: " + trimmedFilter);
			model.addAttribute("page", page);
			model.addAttribute("filter", trimmedFilter);
			return "post/posts";
		} else {
			model.addAttribute("pageTitle", "Incorrect search input.");
			return "error/error-default";
		}
	}
}
