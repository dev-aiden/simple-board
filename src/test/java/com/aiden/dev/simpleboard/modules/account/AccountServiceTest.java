package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.infra.mail.EmailService;
import com.aiden.dev.simpleboard.modules.account.form.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks AccountService accountService;
    @Mock EmailService emailService;
    @Mock AccountRepository accountRepository;
    @Spy PasswordEncoder passwordEncoder;
    @Spy ModelMapper modelMapper;

    @DisplayName("계정 생성 후 메일 발송되는지 테스트")
    @Test
    void processNewAccount() {
        // Given
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setLoginId("test");
        signUpForm.setPassword("testtest");
        signUpForm.setNickname("test");
        signUpForm.setEmail("test@email.com");

        Account account = Account.builder()
                .loginId("test")
                .nickname("test")
                .email("test@email.com")
                .build();

        given(passwordEncoder.encode(any())).willReturn("encryptTest");
        given(accountRepository.save(any())).willReturn(account);

        // When
        Account savedAccount = accountService.processNewAccount(signUpForm);

        // Then
        assertThat(savedAccount.getLoginId()).isEqualTo(signUpForm.getLoginId());
        assertThat(savedAccount.getPassword()).isNotEqualTo(signUpForm.getPassword());
        assertThat(savedAccount.getPassword()).isNotEqualTo("testtest");
        assertThat(savedAccount.getNickname()).isEqualTo(signUpForm.getNickname());
        assertThat(savedAccount.getEmail()).isEqualTo(signUpForm.getEmail());
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
        then(emailService).should().sendEmail(any());
    }

    @DisplayName("로그인 테스트")
    @Test
    void login() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

        // When
        accountService.login(account);

        // Then
        UserAccount authenticationAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

        given(accountRepository.findByLoginId(any())).willReturn(account);

        // When
        UserDetails userDetails = accountService.loadUserByUsername("test");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo("test");
    }

    @DisplayName("이메일 인증하여 회원가입 완료 테스트")
    @Test
    void completeSignUp() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

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
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

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
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

        PasswordForm passwordForm = new PasswordForm();
        passwordForm.setNewPassword("aaaaaaaa");

        // When
        accountService.updatePassword(account, passwordForm.getNewPassword());

        // Then
        assertThat(account.getPassword()).isNotEqualTo("testtest");
    }

    @DisplayName("알림 변경 테스트")
    @Test
    void updateNotification() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .commentNotification(true)
                .build();

        NotificationForm notificationForm = new NotificationForm();
        notificationForm.setCommentNotification(false);

        // When
        accountService.updateNotification(account, notificationForm);

        // Then
        assertThat(account.isCommentNotification()).isEqualTo(false);
    }

    @DisplayName("계정 삭제 테스트")
    @Test
    void deleteAccount() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

        // When
        accountService.deleteAccount(account);

        // Then
        then(accountRepository).should().delete(account);
    }

    @DisplayName("비밀번호 찾기 메일 발송 테스트")
    @Test
    void issueTemporaryPassword() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

        FindPasswordForm findPasswordForm = new FindPasswordForm();
        findPasswordForm.setLoginId("test");
        findPasswordForm.setEmail("test@email.com");

        given(accountRepository.findByLoginId(any())).willReturn(account);

        // When
        accountService.issueTemporaryPassword(findPasswordForm);

        // Then
        assertThat(account.getPassword()).isNotEqualTo("testtest");
        then(emailService).should().sendEmail(any());
    }

    @DisplayName("이메일로 계정 조회 테스트")
    @Test
    void getAccountByEmail() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

        // When
        accountService.getAccountByEmail("test@email.com");

        // Then
        then(accountRepository).should().findByEmail("test@email.com");
    }

    @DisplayName("닉네임으로 계정 조회 테스트")
    @Test
    void getAccountByNickname() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("testtest")
                .nickname("test")
                .email("test@email.com")
                .build();

        // When
        accountService.getAccountByNickname("test");

        // Then
        then(accountRepository).should().findByNickname("test");
    }
}