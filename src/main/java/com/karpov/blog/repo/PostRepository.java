package com.karpov.blog.repo;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post,Long> {
	Iterable<Post> findByAuthor(User user);
}
