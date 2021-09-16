package com.aiden.dev.simpleboard.modules.comment;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.comment.form.WriteCommentForm;
import com.aiden.dev.simpleboard.modules.main.PostService;
import com.aiden.dev.simpleboard.modules.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks CommentService commentService;
    @Mock CommentRepository commentRepository;
    @Mock PostService postService;
    @Mock ApplicationEventPublisher eventPublisher;

    @DisplayName("댓글 작성 테스트 - 존재하지 않는 게시글")
    @Test
    void writeNewComment_not_exist_post() {
        // Given
        WriteCommentForm writeCommentForm = new WriteCommentForm();
        writeCommentForm.setPostId(1L);
        writeCommentForm.setContents("contents");
        writeCommentForm.setSecret(true);

        Account account = Account.builder()
                .loginId("test")
                .password("test")
                .nickname("test")
                .email("test@email.com")
                .build();
        account.generateEmailCheckToken();

        // When, Then
        assertThrows(IllegalArgumentException.class, () -> commentService.writeNewComment(writeCommentForm, account));
    }

    @DisplayName("댓글 작성 테스트")
    @Test
    void writeNewComment() {
        // Given
        Post post = Post.builder().build();

        WriteCommentForm writeCommentForm = new WriteCommentForm();
        writeCommentForm.setPostId(1L);
        writeCommentForm.setContents("contents");
        writeCommentForm.setSecret(true);

        Account account = Account.builder()
                .loginId("test")
                .password("test")
                .nickname("test")
                .email("test@email.com")
                .build();
        account.generateEmailCheckToken();

        given(postService.getPostDetail(any())).willReturn(Optional.of(post));

        // When
        commentService.writeNewComment(writeCommentForm, account);

        // Then
        verify(commentRepository).save(any(Comment.class));
    }

    @DisplayName("postId로 댓글 리스트 조회 테스트")
    @Test
    void getComments() {
        // When
        commentService.getComments(1L);

        // Then
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(1L);
    }

    @DisplayName("commentId로 댓글 조회 테스트")
    @Test
    void getComment() {
        // When
        commentService.getComment(1L);

        // Then
        verify(commentRepository).findById(1L);
    }

    @DisplayName("댓글 삭제 테스트")
    @Test
    void deleteComment() {
        // When
        commentService.deleteComment(new Comment());

        // Then
        verify(commentRepository).delete(any(Comment.class));
    }

    @DisplayName("댓글 수정 테스트")
    @Test
    void updateComment() {
        // Given
        Comment comment = Comment.builder()
                .contents("contents")
                .build();
        given(commentRepository.findById(any())).willReturn(Optional.of(comment));

        // When
        commentService.updateComment(1L, false, "test");

        // Then
        verify(commentRepository).findById(anyLong());
    }
}