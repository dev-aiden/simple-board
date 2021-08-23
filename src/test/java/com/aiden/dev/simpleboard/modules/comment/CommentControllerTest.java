package com.aiden.dev.simpleboard.modules.comment;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.WithAccount;
import com.aiden.dev.simpleboard.modules.comment.form.WriteCommentForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}