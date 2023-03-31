package com.karpov.blog.models;

import jakarta.validation.constraints.Size;

public class Password {

	@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters long")
	private String password;

	public Password() {
	}

	public Password(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
