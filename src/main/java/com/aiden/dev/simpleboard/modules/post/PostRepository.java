package com.aiden.dev.simpleboard.modules.post;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"account"})
    List<Post> findAll();

    Post findByTitle(String title);

    @Override
    @EntityGraph(attributePaths = {"account"})
    Optional<Post> findById(Long id);
}
