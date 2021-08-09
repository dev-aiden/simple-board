package com.aiden.dev.simpleboard;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("API 통합 테스트")
class ApisTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;

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

    @DisplayName("잘못된 입력값으로 회원가입 시 회원가입 실패")
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

    @DisplayName("올바른 입력값으로 회원가입 시 회원가입 성공")
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

    @DisplayName("존재하지 않는 계정에 대한 인증메일 확인 시 인증 실패")
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

    @DisplayName("인증 메일 확인 - 유효하지 않은 토큰")
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

    @DisplayName("인증 메일 확인 - 입력값 정상")
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
    @DisplayName("인증 메일 재발송 확인 - 1시간 이내 재발송")
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
    @DisplayName("인증 메일 재발송 확인 - 1시간 이후 재발송")
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

    @DisplayName("로그인 실패")
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

    @DisplayName("로그인 성공")
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

    @DisplayName("로그아웃")
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
    @DisplayName("프로필 수정 테스트 - 입력값 에러")
    @Test
    void profileUpdate_error_input() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("nickname", "aiden222222222222")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername("aiden"));

        Account aiden = accountRepository.findByLoginId("aiden");
        assertThat(aiden.getNickname()).isEqualTo("aiden");
    }

    @WithUserDetails(value = "aiden", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("프로필 수정 테스트 - 입력값 정상")
    @Test
    void profileUpdate_correct_input() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("nickname", "aiden2")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(authenticated().withUsername("aiden"));

        Account aiden = accountRepository.findByLoginId("aiden");
        assertThat("aiden2").isEqualTo(aiden.getNickname());
    }
}
