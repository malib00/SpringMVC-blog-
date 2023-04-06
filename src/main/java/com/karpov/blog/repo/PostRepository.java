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
	Iterable<Post> findByAuthorInOrderByTimestampDesc(Iterable<User> users);
	Iterable<Post> findFirst3ByAuthor(User user, Sort sort);
	Iterable<Post> findByTitleContainingIgnoreCaseOrderByTimestampDesc(String contain);

	@Query(value = "SELECT count(*) FROM post WHERE user_id =:userId", nativeQuery = true)
	int postsTotal(Long userId);
}
