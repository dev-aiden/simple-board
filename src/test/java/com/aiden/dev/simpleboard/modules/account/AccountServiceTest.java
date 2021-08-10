package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.infra.mail.EmailService;
import com.aiden.dev.simpleboard.modules.account.form.PasswordForm;
import com.aiden.dev.simpleboard.modules.account.form.ProfileForm;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Transactional
@SpringBootTest
class AccountServiceTest {

    @Autowired AccountService accountService;
    @Autowired PasswordEncoder passwordEncoder;
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

    @DisplayName("회원가입 인증 메일 발송 테스트")
    @Test
    void sendSignUpConfirmEmail() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .build();

        // When
        accountService.sendSignUpConfirmEmail(account);

        // Then
        verify(emailService, times(1)).sendEmail(any());
    }

    @DisplayName("로그인 테스트")
    @Test
    void login() {
        // Given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("testtest");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        Account account = accountService.processNewAccount(signUpForm);

        // When
        accountService.login(account);
        UserAccount authenticationAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Then
        assertThat(authenticationAccount.getUsername()).isEqualTo("test");
    }

    @DisplayName("유저 정보 조회 테스트 - 사용자 아이디 미존재")
    @Test
    void loadUserByUsername_not_exist_login_id() {
        // When, Then
        assertThrows(UsernameNotFoundException.class, () -> {
            accountService.loadUserByUsername("test");
        });
    }

    @DisplayName("유저 정보 조회 테스트 - 사용자 아이디 존재")
    @Test
    void loadUserByUsername_exist_login_id() {
        // Given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("testtest");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        accountService.processNewAccount(signUpForm);

        // When
        UserDetails userDetails = accountService.loadUserByUsername("test");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("test");
    }

    @DisplayName("이메일 인증하여 회원가입 완료 테스트")
    @Test
    void completeSignUp() {
        // Given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("testtest");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        Account account = accountService.processNewAccount(signUpForm);

        // When
        accountService.completeSignUp(account);

        // Then
        assertThat(account.isEmailVerified()).isTrue();
        assertThat(account.getJoinedAt()).isNotNull();

        UserAccount authenticationAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(authenticationAccount.getUsername()).isEqualTo("test");
    }

    @DisplayName("프로필 수정 테스트")
    @Test
    void updateProfile() {
        // Given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("testtest");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        Account account = accountService.processNewAccount(signUpForm);

        ProfileForm profileForm = new ProfileForm();
        profileForm.setNickname("test2");

        // When
        accountService.updateProfile(account, profileForm);

        // Then
        assertThat(account.getNickname()).isEqualTo("test2");
    }

    @DisplayName("비밀번호 변경 테스트")
    @Test
    void updatePassword() {
        // Given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("testtest");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");
        Account account = accountService.processNewAccount(signUpForm);
        String originPassword = account.getPassword();

        PasswordForm passwordForm = new PasswordForm();
        passwordForm.setNewPassword("aaaaaaaa");

        // When
        accountService.updatePassword(account, passwordForm.getNewPassword());

        // Then
        assertThat(account.getPassword()).isNotEqualTo(originPassword);
    }
}