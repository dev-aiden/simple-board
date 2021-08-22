package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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

    @DisplayName("ID로 게시글 조회 쿼리 테스트")
    @Test
    void findById() {
        Optional<Post> posts = postRepository.findById(1L);
        assertThat(posts).isNotNull();
    }
}