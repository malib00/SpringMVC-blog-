package com.karpov.blog.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageFile implements Serializable {

	@Id
	private String UUID;

	private String URL;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ImageFile imageFile = (ImageFile) obj;
		return Objects.equals(UUID, imageFile.UUID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(UUID);
	}
}
