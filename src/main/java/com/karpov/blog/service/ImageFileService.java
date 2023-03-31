package com.karpov.blog.service;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class ImageFileService {

	@Value("${upload.path}")
	private String uploadPath;

	public String save(MultipartFile file, String path) throws IOException {
		String fullPath = uploadPath + "/" + path;
		File uploadDir = new File(fullPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}
		String fileName = UUID.randomUUID() + "." + file.getOriginalFilename();
		file.transferTo(new File(fullPath + "/" + fileName));
		return fileName;
	}

	public String replace(MultipartFile file, String path, String oldFileName) throws IOException {
		String fullPath = uploadPath + "/" + path;
		File uploadDir = new File(fullPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}
		String fileName = UUID.randomUUID() + "." + file.getOriginalFilename();
		file.transferTo(new File(fullPath + "/" + fileName));
		Files.deleteIfExists(new File(fullPath + "/" + oldFileName).toPath());
		return fileName;
	}

	public void delete(String path, String oldFileName) throws IOException {
		String fullPath = uploadPath + "/" + path;
		Files.deleteIfExists(new File(fullPath + "/" + oldFileName).toPath());
	}

	public void deleteUserImages(String path) {
			String fullPath = uploadPath + "/" + path;
			File directory = new File(fullPath);
			if (directory.exists()) {
				try {
					FileUtils.deleteDirectory(directory);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
}
