package com.karpov.blog.repo;

import com.karpov.blog.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {
}

