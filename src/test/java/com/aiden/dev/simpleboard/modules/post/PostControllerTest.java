package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.WithAccount;
import com.aiden.dev.simpleboard.modules.main.PostService;
import com.aiden.dev.simpleboard.modules.post.form.WritePostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    @MockBean ModelMapper modelMapper;

    @DisplayName("게시글 작성 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void writePostForm_before_login() throws Exception {
        mockMvc.perform(get("/post/write"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 작성 페이지 보이는지 테스트 - 이메일 인증 전")
    @Test
    void writePostForm_before_email_verify() throws Exception {
        mockMvc.perform(get("/post/write"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("email"));
    }

    @WithAccount(loginId = "aiden", isEmailVerified = true)
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
        Post post = Post.builder()
                .id(1L)
                .build();
        when(postService.writeNewPost(any(), any())).thenReturn(post);

        mockMvc.perform(post("/post/write")
                .param("title", "title")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"));

        verify(postService, times(1)).writeNewPost(any(), any());
    }

    @DisplayName("게시글 상세 페이지 보이는지 테스트 - 존재하지 않는 게시글")
    @Test
    void detailPostForm_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/post/2"))).hasCause(new IllegalArgumentException("2에 해당하는 게시글이 존재하지 않습니다."));
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

        mockMvc.perform(get("/post/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("writeCommentForm"));
    }

    @DisplayName("비공개 게시글 상세 페이지 보이는지 테스트 - 비회원")
    @Test
    void detailPostForm_secret_post_no_member() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .nickname("test")
                .email("test@email.com")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .postType(PostType.PRIVATE)
                .contents("contents")
                .account(account)
                .build();

        when(postService.getPostDetail(any())).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> mockMvc.perform(get("/post/1"))).hasCause(new IllegalArgumentException("게시글 접근 권한이 없습니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비공개 게시글 상세 페이지 보이는지 테스트 - 다른 사용자 게시글")
    @Test
    void detailPostForm_secret_post_other_user() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .nickname("test")
                .email("test@email.com")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .postType(PostType.PRIVATE)
                .contents("contents")
                .account(account)
                .build();

        when(postService.getPostDetail(any())).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> mockMvc.perform(get("/post/1"))).hasCause(new IllegalArgumentException("게시글 접근 권한이 없습니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비공개 게시글 상세 페이지 보이는지 테스트 - 다른 사용자 게시글")
    @Test
    void detailPostForm_secret_post() throws Exception {
        Account account = Account.builder()
                .loginId("aiden")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .postType(PostType.PRIVATE)
                .contents("contents")
                .account(account)
                .build();

        when(postService.getPostDetail(any())).thenReturn(Optional.of(post));

        mockMvc.perform(get("/post/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("writeCommentForm"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 삭제 테스트 - 존재하지 않는 게시글")
    @Test
    void deletePost_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(delete("/post/1").with(csrf())))
                .hasCause(new IllegalArgumentException("1에 해당하는 게시글이 존재하지 않습니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 삭제 테스트 - 다른 사용자 게시글")
    @Test
    void deletePost_other_user_post() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        given(postService.getPostDetail(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> mockMvc.perform(delete("/post/1").with(csrf())))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 삭제 테스트")
    @Test
    void deletePost() throws Exception {
        Account account = Account.builder()
                .loginId("aiden")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        given(postService.getPostDetail(1L)).willReturn(Optional.of(post));

        mockMvc.perform(delete("/post/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 수정 페이지 보이는지 테스트 - 존재하지 않는 게시글")
    @Test
    void updatePostForm_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/post/update/1")))
                .hasCause(new IllegalArgumentException("1에 해당하는 게시글이 존재하지 않습니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 수정 페이지 보이는지 테스트 - 다른 사용자 게시글")
    @Test
    void updatePostForm_other_user_post() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        given(postService.getPostDetail(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> mockMvc.perform(get("/post/update/1")))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 수정 페이지 보이는지 테스트")
    @Test
    void updatePostForm() throws Exception {
        Account account = Account.builder()
                .loginId("aiden")
                .build();

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .postType(PostType.PUBLIC)
                .account(account)
                .build();

        given(postService.getPostDetail(1L)).willReturn(Optional.of(post));
        given(modelMapper.map(any(), any())).willReturn(new WritePostForm());

        mockMvc.perform(get("/post/update/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/update"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("writePostForm"))
                .andExpect(model().attributeExists("post"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 수정 테스트 - 존재하지 않는 게시글")
    @Test
    void updatePost_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(put("/post/update/1").with(csrf())))
                .hasCause(new IllegalArgumentException("1에 해당하는 게시글이 존재하지 않습니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 수정 테스트 - 다른 사용자 게시글")
    @Test
    void updatePost_other_user_post() throws Exception {
        Account account = Account.builder()
                .loginId("test")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        given(postService.getPostDetail(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> mockMvc.perform(put("/post/update/1").with(csrf())))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("게시글 수정 테스트")
    @Test
    void updatePost() throws Exception {
        Account account = Account.builder()
                .loginId("aiden")
                .build();

        Post post = Post.builder()
                .id(1L)
                .account(account)
                .build();

        given(postService.getPostDetail(1L)).willReturn(Optional.of(post));

        mockMvc.perform(put("/post/update/1")
                        .param("title", "title")
                        .param("contents", "contents")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"));

        verify(postService, times(1)).updatePost(any(), any());
    }
}