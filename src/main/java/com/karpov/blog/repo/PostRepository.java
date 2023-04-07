package com.karpov.blog.repo;

import com.karpov.blog.models.Post;
import com.karpov.blog.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post,Long> {
	Page<Post> findAll(Pageable pageable);
	Page<Post> findByAuthor(User user, Pageable pageable);
	Page<Post> findByAuthorIn(Iterable<User> users, Pageable pageable);
	Iterable<Post> findFirst3ByAuthor(User user, Sort sort);

	Page<Post> findByTitleContainingIgnoreCase(String contain, Pageable pageable);

	@Query(value = "SELECT count(*) FROM post WHERE user_id =:userId", nativeQuery = true)
	int postsTotal(Long userId);
}
