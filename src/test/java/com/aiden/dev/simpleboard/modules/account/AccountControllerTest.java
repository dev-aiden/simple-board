package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.modules.account.validator.SignUpFormValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean SignUpFormValidator signUpFormValidator;
    @MockBean AccountService accountService;

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
                .andExpect(view().name("redirect:/"));
    }
}