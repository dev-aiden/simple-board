package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.WithAccount;
import com.aiden.dev.simpleboard.modules.main.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean DataSource dataSource;
    @MockBean PostService postService;

    @DisplayName("게시글 작성 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void writePostForm_before_login() throws Exception {
        mockMvc.perform(get("/post/write"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 작성 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void writePostForm_after_login() throws Exception {
        mockMvc.perform(get("/post/write"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("email"));
    }

    @DisplayName("게시글 작성 처리 - 로그인 이전")
    @Test
    void writePost_before_login() throws Exception {
        mockMvc.perform(post("/post/write")
                .param("title", "")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 작성 처리 - 입력값 오류")
    @Test
    void writePost_with_wrong_input() throws Exception {
        mockMvc.perform(post("/post/write")
                .param("title", "")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/write"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 작성 처리 - 입력값 정상")
    @Test
    void writePost_with_correct_input() throws Exception {
        Post post = Post.builder()
                .id(1L)
                .build();
        when(postService.writeNewPost(any(), any())).thenReturn(post);

        mockMvc.perform(post("/post/write")
                .param("title", "title")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/detail/1"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"));

        verify(postService, times(1)).writeNewPost(any(), any());
    }

    @DisplayName("게시글 상세 페이지 보이는지 테스트 - 존재하지 않는 게시글")
    @Test
    void detailPostForm_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/post/detail/2"))).hasCause(new IllegalArgumentException("2에 해당하는 게시글이 존재하지 않습니다."));
    }

    @DisplayName("게시글 상세 페이지 보이는지 테스트 - 존재하는 게시글")
    @Test
    void detailPostForm_exist_post() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .nickname("test")
                .email("test@email.com")
                .build();

        Post post = Post.builder()
                .title("title")
                .contents("contents")
                .account(account)
                .build();

        when(postService.getPostDetail(any())).thenReturn(Optional.of(post));

        mockMvc.perform(get("/post/detail/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"));
    }
}