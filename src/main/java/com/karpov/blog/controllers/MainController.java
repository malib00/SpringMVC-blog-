package com.karpov.blog.controllers;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;

@Controller
public class MainController {

	@Autowired
	private PostRepository postRepository;

	@Value("${upload.path}")
	private String uploadPath;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home Page");
		Iterable<Post> posts = postRepository.findAll(Sort.by("timestamp").descending());
		model.addAttribute("posts", posts);
		return "home";
	}
}
//@AuthenticationPrincipal User user,

/*		String avatarPath = uploadPath + "/" + user.getId() + "/" + user.getAvatar();
		File avatarFile = new File(avatarPath);
		if (user.getAvatar() == null || user.getAvatar().trim().isEmpty() || !avatarFile.exists() || avatarFile.isDirectory()) {
			user.setAvatar("/static/img/profile-avatar-default.jpg");
		} else {
			user.setAvatar("/img/" + user.getId() + "/" + user.getAvatar());
		}
		model.addAttribute("user", user);*/