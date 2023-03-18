package com.karpov.blog.repo;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post,Long> {
	Iterable<Post> findAll(Sort sort);
	Iterable<Post> findByAuthor(User user, Sort timestamp);
	Iterable<Post> findByAuthorInOrderByTimestampDesc(Iterable<User> users);
	Iterable<Post> findFirst3ByAuthor(User user, Sort sort);
	Iterable<Post> findByTitleContainingIgnoreCaseOrderByTimestampDesc(String contain);
}