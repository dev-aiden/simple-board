package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.modules.account.validator.FindPasswordFormValidator;
import com.aiden.dev.simpleboard.modules.account.validator.SignUpFormValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean SignUpFormValidator signUpFormValidator;
    @MockBean FindPasswordFormValidator findPasswordFormValidator;
    @MockBean Account account;
    @MockBean AccountService accountService;
    @MockBean AccountRepository accountRepository;
    @MockBean DataSource dataSource;

    @DisplayName("회원가입 페이지 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        when(signUpFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        when(signUpFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(post("/sign-up")
                .param("loginId", "test")
                .param("password", "testtest")
                .param("passwordConfirm", "testtest")
                .param("nickname", "test")
                .param("email", "test")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"));
    }

    @DisplayName("회원가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        when(signUpFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(post("/sign-up")
                .param("loginId", "test")
                .param("password", "testtest")
                .param("passwordConfirm", "testtest")
                .param("nickname", "test")
                .param("email", "test@email.com")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @DisplayName("인증 메일 확인 - 존재하지 않는 계정")
    @Test
    void checkEmailToken_with_null_account() throws Exception {
        when(accountRepository.findByEmail(any())).thenReturn(null);

        mockMvc.perform(get("/check-email-token")
                .param("token", "testToken")
                .param("email", "test@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.email"))
                .andExpect(view().name("account/checked-email"));
    }

    @DisplayName("인증 메일 확인 - 유효하지 않은 토큰")
    @Test
    void checkEmailToken_with_wrong_token() throws Exception {
        when(accountRepository.findByEmail(any())).thenReturn(mock(Account.class));
        when(accountRepository.findByEmail(any()).isValidEmailCheckToken(any())).thenReturn(false);

        mockMvc.perform(get("/check-email-token")
                .param("token", "testToken")
                .param("email", "test@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "wrong.token"))
                .andExpect(view().name("account/checked-email"));
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken_with_correct_input() throws Exception {
        when(accountRepository.findByEmail(any())).thenReturn(mock(Account.class));
        when(accountRepository.findByEmail(any()).isValidEmailCheckToken(any())).thenReturn(true);

        mockMvc.perform(get("/check-email-token")
                .param("token", "testToken")
                .param("email", "test@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("account/checked-email"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("인증 메일 페이지 잘 보이는지 확인")
    @Test
    void checkEmail() throws Exception {
        mockMvc.perform(get("/check-email"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("email"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("인증 메일 재발송 확인 - 1시간 이내 재발송")
    @Test
    void resendConfirmEmail_before_1_hour() throws Exception {
        when(account.canSendConfirmEmail()).thenReturn(false);

        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-email"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("email"));
    }

    @WithAccount(loginId = "aiden", minusHoursForEmailCheckToken = 2L)
    @DisplayName("인증 메일 재발송 확인 - 1시간 이후 재발송")
    @Test
    void resendConfirmEmail_after_1_hour() throws Exception {
        mockMvc.perform(get("/resend-confirm-email"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(accountService, times(1)).sendSignUpConfirmEmail(any());
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("아이디로 프로필 페이지 보이는지 확인 - 다른 사용자 아이디")
    @Test
    void viewProfileByLoginId_not_current_account() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/profile/id/aiden2"))).hasCause(new IllegalArgumentException("잘못된 접근입니다."));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("아이디로 프로필 페이지 보이는지 확인 - 본인 아이디")
    @Test
    void viewProfileByLoginId_current_account() throws Exception {
        mockMvc.perform(get("/profile/id/aiden"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/aiden"));
    }

    @DisplayName("닉네임으로 프로필 페이지 보이는지 확인 - 존재하지 않는 사용자")
    @Test
    void viewProfile_not_exist_user() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(get("/profile/aiden"))).hasCause(new IllegalArgumentException("aiden에 해당하는 사용자가 존재하지 않습니다."));
    }

    @DisplayName("닉네임으로 프로필 페이지 보이는지 확인 - 존재하는 사용자")
    @Test
    void viewProfile_exist_user() throws Exception {
        when(accountRepository.findByNickname(any())).thenReturn(new Account());

        mockMvc.perform(get("/profile/aiden"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("isOwner"));
    }

    @DisplayName("비밀번호 찾기 페이지 보이는지 테스트")
    @Test
    void findPasswordForm() throws Exception {
        when(findPasswordFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(get("/find-password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"))
                .andExpect(model().attributeExists("findPasswordForm"));
    }

    @DisplayName("비밀번호 찾기 처리 - 입력값 오류")
    @Test
    void findPasswordSubmit_with_wrong_input() throws Exception {
        when(findPasswordFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(post("/find-password")
                .param("loginId", "test")
                .param("email", "test")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"));
    }

    @DisplayName("비밀번호 찾기 처리 - 입력값 정상")
    @Test
    void findPasswordSubmit_with_correct_input() throws Exception {
        when(findPasswordFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(post("/find-password")
                .param("loginId", "test")
                .param("email", "test@email.com")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"));

        verify(accountService, times(1)).issueTemporaryPassword(any());
    }
}