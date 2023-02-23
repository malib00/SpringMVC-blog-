package com.karpov.blog.repo;

import com.karpov.blog.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
	User findByUsername(String username);

	/*	Optional<User> findByUsername(String username);*/
}

