package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface PostRepository extends JpaRepository<Post, Long> {

    Post findByTitle(String title);

    @Override
    @EntityGraph(attributePaths = {"account"})
    Optional<Post> findById(Long id);

    @EntityGraph(attributePaths = {"comments"})
    Page<Post> findByTitleContains(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"comments"})
    Page<Post> findByAccount_NicknameContains(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"comments"})
    Page<Post> findAll(Pageable pageable);

    void deleteByAccount(Account account);
}
