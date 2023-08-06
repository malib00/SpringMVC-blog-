package com.karpov.blog.service;

import com.karpov.blog.models.ImageFile;
import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.karpov.blog.repo.UserRepository;
import com.uploadcare.upload.UploadFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private ImageFileService imageFileService;

	public void follow(User authenticatedUser, User user) {
		user.getFollowers().add(authenticatedUser);
		userRepository.save(user);
	}

	public void unfollow(User authenticatedUser, User user) {
		user.getFollowers().remove(authenticatedUser);
		userRepository.save(user);
	}

	public boolean sameUsernameFound(String username) {
		User userWithSameUsername = userRepository.findByUsernameIgnoreCase(username);
		return userWithSameUsername != null;
	}

	public Iterable<User> getAllUsers() {
		return userRepository.findAll(Sort.by("id").ascending());
	}

	public Iterable<Post> get3RecentPosts(User user) {
		return postRepository.findFirst3ByAuthor(user, Sort.by("timestamp").descending());
	}

	public User getUserByUsername(String username) {
		return userRepository.findByUsernameIgnoreCase(username);
	}

	public void updateUser(User user, User editedUser, MultipartFile multipartFile) throws UploadFailureException, IOException {

		user.setUsername(editedUser.getUsername());
		user.setFullname(editedUser.getFullname());
		user.setAbout(editedUser.getAbout());
		if (editedUser.getPassword() != null) {
			user.setPassword(passwordEncoder.encode(editedUser.getPassword()));
		}
		user.setEmail(editedUser.getEmail()); //TODO new e-mail activation
		user.setRoles(editedUser.getRoles());
		user.setActive(editedUser.isActive());
		if (multipartFile.isEmpty()) {
			userRepository.save(user);
		} else if (user.getAvatarImage() == null) {
			ImageFile uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
			user.setAvatarImage(uploadedImageFile);
			userRepository.save(user);
			//TODO update authentication principal to display new avatarImage in header.html
		} else {
			ImageFile uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
			ImageFile oldImageFile = user.getAvatarImage();
			user.setAvatarImage(uploadedImageFile);
			userRepository.save(user);
			imageFileService.deleteImageFile(oldImageFile);
			//TODO update authentication principal to display new avatarImage in header.html
		}
	}

	public void deleteUser(User user) {
		List<String> imageUUIDs = new ArrayList<>();
		if (user.getAvatarImage() != null)
		{
			imageUUIDs.add(user.getAvatarImage().getUUID());
		}
		user.getPosts().forEach(post -> imageUUIDs.add(post.getImageFile().getUUID()));
		imageFileService.deleteImages(imageUUIDs);
		userRepository.delete(user);
	}
}
