package com.aiden.dev.simpleboard.modules.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean DataSource dataSource;

    @DisplayName("프로필 수정 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updateProfileForm_not_current_account() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 수정 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updateProfileForm_current_account() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 수정 테스트")
    @Test
    void updateProfile() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("nickname", "aiden2")
                .param("profileImage", "aiden2")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));

        verify(accountService, times(1)).updateProfile(any(), any());
    }

    @DisplayName("비밀번호 수정 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updatePasswordForm_not_current_account() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 수정 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updatePasswordForm_current_account() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비밀번호 변경 테스트")
    @Test
    void updatePassword() throws Exception {
        mockMvc.perform(post("/settings/password")
                .param("newPassword", "11111111")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        verify(accountService, times(1)).updatePassword(any(), any());
    }
}