package com.karpov.blog.service;

import com.karpov.blog.models.ImageFile;
import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.uploadcare.upload.UploadFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
public class PostService {

	@Autowired
	private ImageFileService imageFileService;

	@Autowired
	private PostRepository postRepository;

	public void addNewPost(Post post, MultipartFile multipartFile, User user) throws UploadFailureException, IOException {
		ImageFile uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
		post.setImageFile(uploadedImageFile);
		post.setAuthor(user);
		post.setTimestamp(Instant.now());
		postRepository.save(post);
	}

	public void updatePost(Post post, Post editedPost, MultipartFile multipartFile) throws UploadFailureException, IOException {
		post.setTitle(editedPost.getTitle());
		post.setFulltext(editedPost.getFulltext());
		if (multipartFile.isEmpty()) {
			postRepository.save(post);
		} else {
			ImageFile uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
			ImageFile oldImageFile = post.getImageFile();
			post.setImageFile(uploadedImageFile);
			postRepository.save(post);
			imageFileService.deleteImageFile(oldImageFile);
		}
	}

	public void deletePost(Post post)  {
		ImageFile oldImageFile = post.getImageFile();
		postRepository.delete(post);
		imageFileService.deleteImageFile(oldImageFile);
	}

	public Page<Post> getAllPostsPageable(Pageable pageable) {
		Page<Post> page = postRepository.findAll(pageable.previousOrFirst());
		return page;
	}
}
