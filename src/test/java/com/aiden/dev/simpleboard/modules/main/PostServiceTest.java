package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.post.Post;
import com.aiden.dev.simpleboard.modules.post.PostRepository;
import com.aiden.dev.simpleboard.modules.post.PostType;
import com.aiden.dev.simpleboard.modules.post.WritePostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class PostServiceTest {

    @Autowired PostService postService;
    @Autowired PostRepository postRepository;
    @Autowired AccountRepository accountRepository;

    @DisplayName("게시글 작성 테스트")
    @Test
    void writeNewPost() {
        // Given
        WritePostForm writePostForm = new WritePostForm();
        writePostForm.setTitle("title");
        writePostForm.setSecret(false);
        writePostForm.setContents("contents");

        Account account = Account.builder()
                .loginId("test")
                .password("test")
                .nickname("test")
                .email("test@email.com")
                .build();
        account.generateEmailCheckToken();
        accountRepository.save(account);

        // When
        postService.writeNewPost(writePostForm, account);

        // Then
        Post post = postRepository.findByTitle("title");
        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo("title");
        assertThat(post.getPostType()).isEqualTo(PostType.PUBLIC);
        assertThat(post.getContents()).isEqualTo("contents");
        assertThat(post.getAccount()).isEqualTo(account);
    }
}