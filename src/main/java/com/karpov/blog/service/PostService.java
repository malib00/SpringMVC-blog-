package com.karpov.blog.service;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.uploadcare.upload.UploadFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
public class PostService {

	@Autowired
	private FileStoreService fileStoreService;

	@Autowired
	private PostRepository postRepository;

	public void addPost(Post post, MultipartFile file, User user) throws IOException, UploadFailureException {
		post.setAuthor(user);
		post.setTimestamp(Instant.now());
		String path = String.valueOf(user.getId());
		String fileName = fileStoreService.save(file);
		post.setFilename(fileName);
		postRepository.save(post);
	}
}
