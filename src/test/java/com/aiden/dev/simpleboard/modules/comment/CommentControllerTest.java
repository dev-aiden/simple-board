package com.aiden.dev.simpleboard.modules.comment;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.WithAccount;
import com.aiden.dev.simpleboard.modules.comment.form.WriteCommentForm;
import com.aiden.dev.simpleboard.modules.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean DataSource dataSource;
    @MockBean CommentService commentService;

    @DisplayName("댓글 작성 처리 - 로그인 이전")
    @Test
    void writeComment_before_login() throws Exception {
        mockMvc.perform(post("/comment/write")
                        .param("contents", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("댓글 작성 처리 - 이메일 인증 전")
    @Test
    void writeComment_before_email_verification() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/comment/write")
                        .param("postId", "1")
                        .param("contents", "contents")
                        .with(csrf()))).hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden", isEmailVerified = true)
    @DisplayName("댓글 작성 처리 - 입력값 오류")
    @Test
    void writeComment_wrong_input() throws Exception {
        mockMvc.perform(post("/comment/write")
                        .param("postId", "1")
                        .param("contents", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"));
    }

    @WithAccount(loginId = "aiden", isEmailVerified = true)
    @DisplayName("댓글 작성 처리")
    @Test
    void writeComment() throws Exception {
        mockMvc.perform(post("/comment/write")
                        .param("postId", "1")
                        .param("contents", "contents")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"));

        verify(commentService).writeNewComment(any(WriteCommentForm.class), any(Account.class));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("댓글 삭제 테스트 - 존재하지 않는 댓글")
    @Test
    void deleteComment_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(delete("/comment/1").with(csrf())))
                .hasCause(new IllegalArgumentException("1에 해당하는 댓글이 존재하지 않습니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("댓글 삭제 테스트 - 다른 사용자 댓글")
    @Test
    void deleteComment_other_user_post() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        Comment comment = Comment.builder()
                .post(post)
                .account(account)
                .build();

        given(commentService.getComment(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> mockMvc.perform(delete("/comment/1").with(csrf())))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("댓글 삭제 테스트")
    @Test
    void deleteComment() throws Exception {
        Account account = Account.builder()
                .loginId("aiden")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        Comment comment = Comment.builder()
                .post(post)
                .account(account)
                .build();

        given(commentService.getComment(1L)).willReturn(Optional.of(comment));

        mockMvc.perform(delete("/comment/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("댓글 수정 테스트 - 존재하지 않는 댓글")
    @Test
    void updateComment_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(put("/comment/1").with(csrf())))
                .hasCause(new IllegalArgumentException("1에 해당하는 댓글이 존재하지 않습니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("댓글 수정 테스트 - 다른 사용자 댓글")
    @Test
    void updateComment_other_user_post() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .post(post)
                .account(account)
                .build();

        given(commentService.getComment(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> mockMvc.perform(put("/comment/1").with(csrf())))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("댓글 수정 테스트")
    @Test
    void updateComment() throws Exception {
        Account account = Account.builder()
                .loginId("aiden")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .post(post)
                .account(account)
                .build();

        given(commentService.getComment(1L)).willReturn(Optional.of(comment));

        mockMvc.perform(put("/comment/1")
                        .param("updateSecret", "true")
                        .param("updateContents", "contents")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"));

        verify(commentService).updateComment(anyLong(), anyBoolean(), anyString());
    }
}