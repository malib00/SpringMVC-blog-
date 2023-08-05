package com.karpov.blog.service;

import com.karpov.blog.models.ImageFile;
import com.uploadcare.api.Client;
import com.uploadcare.api.File;
import com.uploadcare.upload.FileUploader;
import com.uploadcare.upload.UploadFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ImageFileService {

	@Autowired
	private Client uploadCareClient;

	public ImageFile uploadImageFile(MultipartFile multipartFile) throws IOException, UploadFailureException {
		FileUploader uploader = new FileUploader(uploadCareClient, multipartFile.getInputStream(), multipartFile.getOriginalFilename());
		File uploadedImageFile = uploader.upload().save();
		ImageFile imageFile = new ImageFile();
		imageFile.setUUID(uploadedImageFile.getFileId());
		imageFile.setURL(uploadedImageFile.getOriginalFileUrl().toString());
		return imageFile;
	}

	public void deleteImageFile(ImageFile imageFile) {
		uploadCareClient.deleteFile(imageFile.getUUID());
	}

	public void deleteImages(List<String> UUIDs) {
		UUIDs.forEach(uuid -> uploadCareClient.deleteFile(uuid));
	}
}
