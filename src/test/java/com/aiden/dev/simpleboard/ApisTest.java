package com.aiden.dev.simpleboard;

import com.aiden.dev.simpleboard.infra.mail.EmailService;
import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    @MockBean EmailService emailService;

    @BeforeEach
    void beforeEach() {
        Account account = Account.builder()
                .loginId("aiden")
                .password("aiden1234")
                .nickname("aiden")
                .email("aiden@email.com")
                .emailCheckToken("aidenToken")
                .build();
        accountRepository.save(account);
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
                .andExpect(view().name("account/sign-up"));

        assertThat(accountRepository.existsByEmail("test@email.com")).isFalse();
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
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("test@email.com");
        assertThat(account).isNotNull();
        assertThat(account.getPassword()).isNotEqualTo("testtest");
        assertThat(account.getEmailCheckToken()).isNotNull();
        then(emailService).should().sendEmail(any());
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
                .andExpect(view().name("account/checked-email"));
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
                .andExpect(view().name("account/checked-email"));
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken_with_correct_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                    .param("token", "aidenToken")
                    .param("email", "aiden@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(view().name("account/checked-email"));

        Account account = accountRepository.findByEmail("aiden@email.com");
        assertThat(account).isNotNull();
        assertThat(account.isEmailVerified()).isTrue();
        assertThat(account.getJoinedAt()).isNotNull();
    }
}
