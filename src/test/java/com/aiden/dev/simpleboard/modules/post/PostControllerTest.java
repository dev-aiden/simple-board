package com.aiden.dev.simpleboard.modules.post;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(view().name("post/write"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("writePostForm"));
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
        mockMvc.perform(post("/post/write")
                .param("title", "title")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"));

        verify(postService, times(1)).writeNewPost(any(), any());
    }
}