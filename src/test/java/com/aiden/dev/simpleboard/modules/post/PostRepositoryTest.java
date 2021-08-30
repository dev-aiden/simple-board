package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired PostRepository postRepository;
    @Autowired AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {
        Account account = Account.builder()
                .loginId("test")
                .password("test")
                .nickname("test")
                .email("test@email.com")
                .build();
        account.generateEmailCheckToken();
        accountRepository.save(account);

        Post post = Post.builder()
                .id(1L)
                .title("post test")
                .account(account)
                .build();
        postRepository.save(post);
    }

    @DisplayName("모든 게시글 조회 쿼리 테스트")
    @Test
    void findAll() {
        List<Post> posts = postRepository.findAll();
        assertThat(posts.size()).isEqualTo(1);
    }

    @DisplayName("제목으로 게시글 조회 쿼리 테스트")
    @Test
    void findByTitle() {
        Post post = postRepository.findByTitle("post test");
        assertThat(post).isNotNull();
    }

    @DisplayName("제목으로 게시글 조회 쿼리 테스트")
    @Test
    void findByTitleContains() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> posts = postRepository.findByTitleContains("po", pageable);

        assertThat(posts.getNumber()).isEqualTo(0);
        assertThat(posts.getSize()).isEqualTo(10);
        assertThat(posts.getTotalPages()).isEqualTo(1);
        assertThat(posts.getNumberOfElements()).isEqualTo(1);
        assertThat(posts.getTotalElements()).isEqualTo(1L);
        assertThat(posts.hasPrevious()).isEqualTo(false);
        assertThat(posts.isFirst()).isEqualTo(true);
        assertThat(posts.hasNext()).isEqualTo(false);
        assertThat(posts.isLast()).isEqualTo(true);
    }

    @DisplayName("작성자로 게시글 조회 쿼리 테스트")
    @Test
    void findByAccount_NicknameContains() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> posts = postRepository.findByAccount_NicknameContains("es", pageable);

        assertThat(posts.getNumber()).isEqualTo(0);
        assertThat(posts.getSize()).isEqualTo(10);
        assertThat(posts.getTotalPages()).isEqualTo(1);
        assertThat(posts.getNumberOfElements()).isEqualTo(1);
        assertThat(posts.getTotalElements()).isEqualTo(1L);
        assertThat(posts.hasPrevious()).isEqualTo(false);
        assertThat(posts.isFirst()).isEqualTo(true);
        assertThat(posts.hasNext()).isEqualTo(false);
        assertThat(posts.isLast()).isEqualTo(true);
    }
}