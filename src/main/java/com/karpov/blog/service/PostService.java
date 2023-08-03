package com.karpov.blog.service;

import com.karpov.blog.models.ImageFile;
import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.PostRepository;
import com.uploadcare.api.File;
import com.uploadcare.upload.UploadFailureException;
import org.springframework.beans.factory.annotation.Autowired;
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
		File uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
		ImageFile imageFile = new ImageFile();
		imageFile.setPost(post);
		imageFile.setUUID(uploadedImageFile.getFileId());
		imageFile.setURL(uploadedImageFile.getOriginalFileUrl().toString());

		post.setImageFile(imageFile);
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
			File uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
			String oldImageFileUUID = post.getImageFile().getUUID();
			ImageFile imageFile = new ImageFile();
			imageFile.setId(post.getId());
			imageFile.setPost(post);
			imageFile.setUUID(uploadedImageFile.getFileId());
			imageFile.setURL(uploadedImageFile.getOriginalFileUrl().toString());
			post.setImageFile(imageFile);
			postRepository.save(post);
			imageFileService.deleteImageFile(oldImageFileUUID);
		}
	}

	public void deletePost(Post post)  {
		String oldImageFileUUID = post.getImageFile().getUUID();
		postRepository.delete(post);
		imageFileService.deleteImageFile(oldImageFileUUID);
	}
}
