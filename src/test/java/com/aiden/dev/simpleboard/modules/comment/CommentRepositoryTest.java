package com.aiden.dev.simpleboard.modules.comment;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.post.Post;
import com.aiden.dev.simpleboard.modules.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired AccountRepository accountRepository;
    @Autowired PostRepository postRepository;
    @Autowired CommentRepository commentRepository;

    Long postId;

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
        Post savedPost = postRepository.save(post);
        postId = savedPost.getId();

        Comment comment = Comment.builder()
                .account(account)
                .post(savedPost)
                .contents("content")
                .build();
        commentRepository.save(comment);
    }

    @DisplayName("postId로 댓글 조회 쿼리 테스트")
    @Test
    void findByPostId() {
        assertThat(commentRepository.findByPostId(postId).size()).isEqualTo(1);
    }
}