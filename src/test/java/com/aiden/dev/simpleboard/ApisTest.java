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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

        Optional<Account> optionalAccount = accountRepository.findByNickname("test");
        assertThat(optionalAccount.isPresent()).isTrue();
        Account account = optionalAccount.get();
        assertThat(account.getPassword()).isNotEqualTo("testtest");
        assertThat(account.getEmailCheckToken()).isNotNull();
        then(emailService).should().sendEmail(any());
    }
}
