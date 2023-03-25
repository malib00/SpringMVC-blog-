package com.karpov.blog.repo;

import com.karpov.blog.models.Article;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

public interface ArticleRepository extends CrudRepository<Article,Long> {
	Iterable<Article> findAll(Sort sort);
	Article findTopByOrderByTimestampDesc();
}
