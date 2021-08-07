package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.infra.mail.EmailService;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@SpringBootTest
class AccountServiceTest {

    @Autowired AccountService accountService;
    @MockBean EmailService emailService;

    @DisplayName("계정 생성 후 메일 발송되는지 테스트")
    @Test
    void processNewAccount() {
        // Given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("testtest");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");

        // When
        Account account = accountService.processNewAccount(signUpForm);

        // Then
        assertThat(account.getLoginId()).isEqualTo(signUpForm.getLoginId());
        assertThat(account.getPassword()).isEqualTo(signUpForm.getPassword());
        assertThat(account.getPassword()).isNotEqualTo("testtest");
        assertThat(account.getNickname()).isEqualTo(signUpForm.getNickname());
        assertThat(account.getEmail()).isEqualTo(signUpForm.getEmail());
        assertThat(account.getEmailCheckToken()).isNotNull();
        then(emailService).should().sendEmail(any());
    }

    @DisplayName("로그인 테스트")
    @Test
    void login() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .build();

        // When
        accountService.login(account);
        UserAccount authenticationAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Then
        assertThat(authenticationAccount.getUsername()).isEqualTo("test");
        assertThat(authenticationAccount.getPassword()).isEqualTo("testtest");
    }
}