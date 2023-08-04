package com.karpov.blog.service;

import com.karpov.blog.models.Article;
import com.karpov.blog.models.ImageFile;
import com.karpov.blog.models.User;
import com.karpov.blog.repo.ArticleRepository;
import com.uploadcare.upload.UploadFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
public class ArticleService {

	@Autowired
	private ImageFileService imageFileService;

	@Autowired
	private ArticleRepository articleRepository;

	public Article getRecentArticle() {
		return articleRepository.findTopByOrderByTimestampDesc();
	}

	public Iterable<Article> getAllArticles() {
		return articleRepository.findAll(Sort.by("timestamp").descending());
	}

	public void addNewArticle(Article article, MultipartFile multipartFile, User user) throws UploadFailureException, IOException {
		ImageFile uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
		article.setImageFile(uploadedImageFile);
		article.setAuthor(user);
		article.setTimestamp(Instant.now());
		articleRepository.save(article);
	}

	public void updateArticle(Article article, Article editedArticle, MultipartFile multipartFile) throws UploadFailureException, IOException {
		article.setTitle(editedArticle.getTitle());
		article.setFulltext(editedArticle.getFulltext());
		if (multipartFile.isEmpty()) {
			articleRepository.save(article);
		} else {
			ImageFile uploadedImageFile = imageFileService.uploadImageFile(multipartFile);
			ImageFile oldImageFile = article.getImageFile();
			article.setImageFile(uploadedImageFile);
			articleRepository.save(article);
			imageFileService.deleteImageFile(oldImageFile);
		}
	}

	public void deleteArticle(Article article)  {
		ImageFile oldImageFile = article.getImageFile();
		articleRepository.delete(article);
		imageFileService.deleteImageFile(oldImageFile);
	}
}
