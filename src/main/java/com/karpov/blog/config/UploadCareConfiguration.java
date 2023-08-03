package com.karpov.blog.config;

import com.uploadcare.api.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadCareConfiguration {

	@Value("${uploadcare.publickey}")
	private String uploadCarePublicKey;

	@Value("${uploadcare.secretkey}")
	private String uploadCareSecretKey;

	@Bean
	public Client client() {
	return new Client(uploadCarePublicKey, uploadCareSecretKey);
	}
}
