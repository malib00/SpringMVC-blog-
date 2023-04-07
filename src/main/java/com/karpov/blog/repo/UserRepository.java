package com.karpov.blog.repo;

import com.karpov.blog.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
	Iterable<User> findAll(Sort sort);
	User findByUsername(String username);
	Page<User> findByUsernameContainingIgnoreCase(String contain, Pageable pageable);
	User findByEmailActivationCode(String code);
}
