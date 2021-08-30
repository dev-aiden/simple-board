package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.main.PostService;
import com.aiden.dev.simpleboard.modules.post.form.WritePostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks PostService postService;
    @Mock PostRepository postRepository;
    @Spy ModelMapper modelMapper;

    @DisplayName("모든 게시글 조회 테스트")
    @Test
    void getAllPost() {
        // When
        postService.getAllPost(PageRequest.of(0, 10));

        // Then
        verify(postRepository).findAll(any(PageRequest.class));
    }

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

        // When
        postService.writeNewPost(writePostForm, account);

        // Then
        verify(postRepository).save(any(Post.class));
    }

    @DisplayName("게시글 상세 정보 조회 테스트")
    @Test
    void getPostDetail() {
        // When
        postService.getPostDetail(1L);

        // Then
        verify(postRepository).findById(anyLong());
    }

    @DisplayName("게시글 삭제 테스트")
    @Test
    void deletePost() {
        // When
        postService.deletePost(1L);

        // Then
        verify(postRepository).deleteById(anyLong());
    }

    @DisplayName("게시글 수정 테스트")
    @Test
    void updatePost() {
        // Given
        Post post = Post.builder()
                .title("title")
                .build();
        given(postRepository.findById(any())).willReturn(Optional.of(post));

        // When
        postService.updatePost(1L, new WritePostForm());

        // Then
        verify(postRepository).findById(anyLong());
    }

    @DisplayName("조회수 증가 테스트")
    @Test
    void increaseHits() {
        // Given
        Post post = Post.builder()
                .title("title")
                .hits(0L)
                .build();

        // When
        postService.increaseHits(post);

        // Then
        assertThat(post.getHits()).isEqualTo(1);
    }
}