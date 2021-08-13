package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

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
}