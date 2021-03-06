package com.aiden.dev.simpleboard;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import com.aiden.dev.simpleboard.modules.comment.Comment;
import com.aiden.dev.simpleboard.modules.comment.CommentRepository;
import com.aiden.dev.simpleboard.modules.comment.CommentType;
import com.aiden.dev.simpleboard.modules.notification.Notification;
import com.aiden.dev.simpleboard.modules.notification.NotificationRepository;
import com.aiden.dev.simpleboard.modules.post.Post;
import com.aiden.dev.simpleboard.modules.post.PostRepository;
import com.aiden.dev.simpleboard.modules.post.PostType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("API ?????? ?????????")
class ApisTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;
    @Autowired PostRepository postRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired NotificationRepository notificationRepository;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("aiden");
        signUpForm.setNickname("aiden");
        signUpForm.setEmail("aiden@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("index ????????? ?????????")
    @Test
    void home() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(view().name("index"))
                .andExpect(unauthenticated());
    }

    @DisplayName("????????? ??????????????? ???????????? ??? ???????????? ??????")
    @Test
    void signUp_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                    .param("loginId", "test")
                    .param("password", "testtest")
                    .param("passwordConfirm", "testtest")
                    .param("nickname", "test")
                    .param("email", "test")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());

        assertThat(accountRepository.existsByLoginId("test")).isFalse();
    }

    @DisplayName("????????? ??????????????? ???????????? ??? ???????????? ??????")
    @Test
    void signUp_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                    .param("loginId", "test")
                    .param("password", "testtest")
                    .param("passwordConfirm", "testtest")
                    .param("nickname", "test")
                    .param("email", "test@email.com")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test"));

        Account account = accountRepository.findByLoginId("test");
        assertThat(account).isNotNull();
        assertThat(account.getPassword()).isNotEqualTo("testtest");
        assertThat(account.getNickname()).isEqualTo("test");
        assertThat(account.getEmail()).isEqualTo("test@email.com");
        assertThat(account.getEmailCheckToken()).isNotNull();
    }

    @DisplayName("???????????? ?????? ????????? ?????? ???????????? ?????? ??? ?????? ??????")
    @Test
    void checkEmailToken_with_null_account() throws Exception {
        mockMvc.perform(get("/check-email-token")
                    .param("token", "testToken")
                    .param("email", "test@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.email"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("?????? ?????? ?????? - ???????????? ?????? ??????")
    @Test
    void checkEmailToken_with_wrong_token() throws Exception {
        mockMvc.perform(get("/check-email-token")
                    .param("token", "testToken")
                    .param("email", "aiden@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.token"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    @Test
    void checkEmailToken_with_correct_input() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        mockMvc.perform(get("/check-email-token")
                    .param("token", account.getEmailCheckToken())
                    .param("email", "aiden@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(account.isEmailVerified()).isTrue();
        assertThat(account.getJoinedAt()).isNotNull();
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ????????? ?????? - 1?????? ?????? ?????????")
    @Test
    void resendConfirmEmail_before_1_hour() throws Exception {
        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("email"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ????????? ?????? - 1?????? ?????? ?????????")
    @Test
    void resendConfirmEmail_after_1_hour() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");
        account.setEmailCheckTokenGeneratedAt(LocalDateTime.now().minusHours(2L));

        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("????????? ??????")
    @Test
    void login_fail() throws Exception {
        mockMvc.perform(post("/login")
                    .param("username", "aiden2")
                    .param("password", "11111111")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("????????? ??????")
    @Test
    void login_success() throws Exception {
        mockMvc.perform(post("/login")
                    .param("username", "aiden")
                    .param("password", "12345678")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("????????????")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ????????? - ????????? ??????")
    @Test
    void updateProfile_error_input() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("nickname", "aiden222222222222")
                .param("profileImage", "aiden")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername("aiden"));

        Account aiden = accountRepository.findByLoginId("aiden");
        assertThat(aiden.getNickname()).isEqualTo("aiden");
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ????????? - ????????? ??????")
    @Test
    void updateProfile_correct_input() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("nickname", "aiden2")
                .param("profileImage", "aiden2")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated().withUsername("aiden"));

        Account aiden = accountRepository.findByLoginId("aiden");
        assertThat("aiden2").isEqualTo(aiden.getNickname());
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ????????? - ????????? ??????")
    @Test
    void updatePassword_error_input() throws Exception {
        Account aiden = accountRepository.findByLoginId("aiden");
        String originPassword = aiden.getPassword();

        mockMvc.perform(post("/settings/password")
                .param("newPassword", "11111111")
                .param("newPasswordConfirm", "11111112")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(aiden.getPassword()).isEqualTo(originPassword);
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ????????? - ????????? ??????")
    @Test
    void updatePassword_correct_input() throws Exception {
        Account aiden = accountRepository.findByLoginId("aiden");
        String originPassword = aiden.getPassword();

        mockMvc.perform(post("/settings/password")
                .param("newPassword", "11111111")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(aiden.getPassword()).isNotEqualTo(originPassword);
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ?????????")
    @Test
    void updateNotification() throws Exception {
        Account aiden = accountRepository.findByLoginId("aiden");
        boolean originNotification = aiden.isCommentNotification();

        mockMvc.perform(post("/settings/notification")
                .param("commentNotification", String.valueOf(!originNotification))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notification"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(aiden.isCommentNotification()).isNotEqualTo(originNotification);
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ?????????")
    @Test
    void deleteAccount() throws Exception {
        mockMvc.perform(delete("/settings/account")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(unauthenticated());

        assertThat(accountRepository.findByNickname("aiden")).isNull();
    }

    @DisplayName("????????? ??????????????? ???????????? ?????? ??? ???????????? ?????? ??????")
    @Test
    void findPassword_wrong_input() throws Exception {
        mockMvc.perform(post("/find-password")
                .param("loginId", "aiden")
                .param("email", "aiden")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"))
                .andExpect(unauthenticated());

        assertThat(accountRepository.existsByLoginId("test")).isFalse();
    }

    @DisplayName("????????? ??????????????? ???????????? ?????? ??? ???????????? ?????? ??????")
    @Test
    void findPassword_correct_input() throws Exception {
        String originPassword = accountRepository.findByLoginId("aiden").getPassword();

        mockMvc.perform(post("/find-password")
                .param("loginId", "aiden")
                .param("email", "aiden@email.com")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(unauthenticated());

        assertThat(accountRepository.findByLoginId("aiden").getPassword()).isNotEqualTo(originPassword);
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ??????????????? ????????? ?????? ??? ????????? ?????? ??????")
    @Test
    void writePost_wrong_input() throws Exception {
        mockMvc.perform(post("/post/write")
                .param("title", "")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/write"))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(postRepository.findByTitle("")).isNull();
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ??????????????? ????????? ?????? ??? ????????? ?????? ??????")
    @Test
    void writePost_correct_input() throws Exception {
        mockMvc.perform(post("/post/write")
                .param("title", "title")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(postRepository.findByTitle("title")).isNotNull();
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ???????????? ????????? ???????????? ?????? ??? ??????")
    @Test
    void detailPostForm_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/post/1")
                        .with(csrf()))).hasCause(new IllegalArgumentException("1??? ???????????? ???????????? ???????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ???????????? ????????? ???????????? ?????? ??? ??????")
    @Test
    void detailPostForm_exist_post() throws Exception {
        Account aiden = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .title("title")
                .contents("contents")
                .account(aiden)
                .hits(0L)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(get("/post/" + savedPost.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("writeCommentForm"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @DisplayName("???????????? ????????? ????????? ???????????? ?????? ??? ??????")
    @Test
    void detailPostForm_secret_post_no_member() throws Exception {
        Account aiden = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .title("title")
                .postType(PostType.PRIVATE)
                .contents("contents")
                .account(aiden)
                .build();
        Post savedPost = postRepository.save(post);

        assertThatThrownBy(() -> mockMvc.perform(get("/post/" + savedPost.getId())
                .with(csrf()))).hasCause(new IllegalArgumentException("????????? ?????? ????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ???????????? ????????? ????????? ???????????? ?????? ??? ??????")
    @Test
    void detailPostForm_secret_post_other_user() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        signUpForm.setPassword("testtest");
        Account account = accountService.processNewAccount(signUpForm);

        Post post = Post.builder()
                .title("title")
                .postType(PostType.PRIVATE)
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        assertThatThrownBy(() -> mockMvc.perform(get("/post/" + savedPost.getId())
                .with(csrf()))).hasCause(new IllegalArgumentException("????????? ?????? ????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ????????? ????????? ???????????? ?????? ??? ??????")
    @Test
    void detailPostForm_secret_post() throws Exception {
        Account aiden = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .title("title")
                .postType(PostType.PRIVATE)
                .contents("contents")
                .account(aiden)
                .hits(0L)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(get("/post/" + savedPost.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("writeCommentForm"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ????????? ?????? ??? ??????")
    @Test
    void deletePost_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(delete("/post/1")
                .with(csrf()))).hasCause(new IllegalArgumentException("1??? ???????????? ???????????? ???????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ????????? ????????? ?????? ??? ??????")
    @Test
    void deletePost_other_user_post() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        signUpForm.setPassword("testtest");
        Account account = accountService.processNewAccount(signUpForm);

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        assertThatThrownBy(() -> mockMvc.perform(delete("/post/" + savedPost.getId())
                .with(csrf()))).hasCause(new IllegalArgumentException("????????? ???????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ??????")
    @Test
    void deletePost() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(delete("/post/" + savedPost.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ???????????? ?????? ????????? ?????? ??? ??????")
    @Test
    void updatePostForm_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/post/1/update")
                .with(csrf()))).hasCause(new IllegalArgumentException("1??? ???????????? ???????????? ???????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ????????? ???????????? ?????? ????????? ?????? ??? ??????")
    @Test
    void updatePostForm_exist_post() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        signUpForm.setPassword("testtest");
        Account account = accountService.processNewAccount(signUpForm);

        Post post = Post.builder()
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        assertThatThrownBy(() -> mockMvc.perform(get("/post/" + savedPost.getId() + "/update")
                .with(csrf()))).hasCause(new IllegalArgumentException("????????? ???????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ????????? ?????? ??????")
    @Test
    void updatePostForm() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(get("/post/" + savedPost.getId() + "/update")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("post/update"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("writePostForm"))
                .andExpect(model().attributeExists("post"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ????????? ?????? ?????? ??? ??????")
    @Test
    void updatePost_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(put("/post/1")
                .with(csrf()))).hasCause(new IllegalArgumentException("1??? ???????????? ???????????? ???????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ????????? ????????? ?????? ?????? ??? ??????")
    @Test
    void updatePost_exist_post() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        signUpForm.setPassword("testtest");
        Account account = accountService.processNewAccount(signUpForm);

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        assertThatThrownBy(() -> mockMvc.perform(put("/post/" + savedPost.getId())
                .with(csrf()))).hasCause(new IllegalArgumentException("????????? ???????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Form ?????? ??? ????????? ?????? ??????")
    @Test
    void updatePost_form_error() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(put("/post/" + savedPost.getId())
                        .param("title", "")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/" + savedPost.getId()))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(postRepository.findById(savedPost.getId()).get().getTitle()).isEqualTo("title");
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ?????? ??????")
    @Test
    void updatePost() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(put("/post/" + savedPost.getId())
                        .param("title", "title2")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/" + savedPost.getId()))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(postRepository.findById(savedPost.getId()).get().getTitle()).isEqualTo("title2");
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ??????????????? ?????? ?????? ??? ?????? ?????? ??????")
    @Test
    void writeComment_wrong_input() throws Exception {
        mockMvc.perform(post("/comment/write")
                        .param("postId", "1")
                        .param("contents", "")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("????????? ??????????????? ????????? ?????? ??? ????????? ?????? ??????")
    @Test
    void writeComment_correct_input() throws Exception {
        Account aiden = accountRepository.findByLoginId("aiden");
        aiden.setEmailVerified(true);

        Post post = Post.builder()
                .title("title")
                .contents("contents")
                .account(aiden)
                .build();
        Post savedPost = postRepository.save(post);

        mockMvc.perform(post("/comment/write")
                        .param("postId", String.valueOf(post.getId()))
                        .param("contents", "contents")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/" + savedPost.getId()))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(commentRepository.findAll().size()).isEqualTo(1);
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ?????? ?????? ??? ??????")
    @Test
    void deleteComment_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(delete("/comment/1")
                .with(csrf()))).hasCause(new IllegalArgumentException("1??? ???????????? ????????? ???????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ????????? ?????? ?????? ??? ??????")
    @Test
    void deleteComment_other_user_post() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        signUpForm.setPassword("testtest");
        Account account = accountService.processNewAccount(signUpForm);

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        Comment comment = Comment.builder()
                .account(account)
                .post(savedPost)
                .commentType(CommentType.PUBLIC)
                .contents("contents")
                .build();
        Comment savedComment = commentRepository.save(comment);

        assertThatThrownBy(() -> mockMvc.perform(delete("/comment/" + savedComment.getId())
                .with(csrf()))).hasCause(new IllegalArgumentException("????????? ???????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ??????")
    @Test
    void deleteComment() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        Comment comment = Comment.builder()
                .account(account)
                .post(savedPost)
                .commentType(CommentType.PUBLIC)
                .contents("contents")
                .build();
        Comment savedComment = commentRepository.save(comment);

        mockMvc.perform(delete("/comment/" + savedComment.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/" + savedPost.getId()))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ?????? ?????? ??? ??????")
    @Test
    void updateComment_not_exist_post() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(put("/comment/1")
                .with(csrf()))).hasCause(new IllegalArgumentException("1??? ???????????? ????????? ???????????? ????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ????????? ?????? ?????? ??? ??????")
    @Test
    void updateComment_other_user_post() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        signUpForm.setPassword("testtest");
        Account account = accountService.processNewAccount(signUpForm);

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        Comment comment = Comment.builder()
                .account(account)
                .post(savedPost)
                .commentType(CommentType.PUBLIC)
                .contents("contents")
                .build();
        Comment savedComment = commentRepository.save(comment);

        assertThatThrownBy(() -> mockMvc.perform(put("/comment/" + savedComment.getId())
                .with(csrf()))).hasCause(new IllegalArgumentException("????????? ???????????????."));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ??????")
    @Test
    void updateComment() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        Comment comment = Comment.builder()
                .account(account)
                .post(savedPost)
                .commentType(CommentType.PUBLIC)
                .contents("contents")
                .build();
        Comment savedComment = commentRepository.save(comment);

        mockMvc.perform(put("/comment/" + savedComment.getId())
                        .param("updateSecret", "true")
                        .param("updateContents", "updateContents")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/" + savedPost.getId()))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(savedComment.getCommentType()).isEqualTo(CommentType.PRIVATE);
        assertThat(savedComment.getContents()).isEqualTo("updateContents");
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("???????????? ?????? ?????? ??????")
    @Test
    void getNotifications() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        Comment comment = Comment.builder()
                .account(account)
                .post(savedPost)
                .commentType(CommentType.PUBLIC)
                .contents("contents")
                .build();
        commentRepository.save(comment);

        mockMvc.perform(get("/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notification/list"))
                .andExpect(model().attributeExists("isNew"))
                .andExpect(model().attributeExists("numberOfNotChecked"))
                .andExpect(model().attributeExists("numberOfChecked"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ?????? ??????")
    @Test
    void getOldNotifications() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Post post = Post.builder()
                .id(1L)
                .title("title")
                .contents("contents")
                .account(account)
                .build();
        Post savedPost = postRepository.save(post);

        Comment comment = Comment.builder()
                .account(account)
                .post(savedPost)
                .commentType(CommentType.PUBLIC)
                .contents("contents")
                .build();
        commentRepository.save(comment);

        mockMvc.perform(get("/notifications/old"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notification/list"))
                .andExpect(model().attributeExists("isNew"))
                .andExpect(model().attributeExists("numberOfNotChecked"))
                .andExpect(model().attributeExists("numberOfChecked"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(authenticated().withUsername("aiden"));
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????? ?????? ??????")
    @Test
    void deleteNotifications() throws Exception {
        Account account = accountRepository.findByLoginId("aiden");

        Notification notification = new Notification();
        notification.setAccount(account);
        notification.setChecked(true);
        notificationRepository.save(notification);

        mockMvc.perform(delete("/notifications")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"))
                .andExpect(authenticated().withUsername("aiden"));

        assertThat(notificationRepository.countByAccountAndChecked(account, true)).isEqualTo(0L);
    }
}
