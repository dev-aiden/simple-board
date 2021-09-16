package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.modules.account.form.NotificationForm;
import com.aiden.dev.simpleboard.modules.account.form.ProfileForm;
import com.aiden.dev.simpleboard.modules.account.validator.PasswordFormValidator;
import com.aiden.dev.simpleboard.modules.account.validator.ProfileFormValidator;
import com.aiden.dev.simpleboard.modules.notification.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean NotificationRepository notificationRepository;
    @MockBean PasswordFormValidator passwordFormValidator;
    @MockBean ProfileFormValidator profileFormValidator;
    @MockBean AccountService accountService;
    @MockBean DataSource dataSource;
    @MockBean ModelMapper modelMapper;

    @DisplayName("프로필 변경 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updateProfileForm_before_login() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 변경 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updateProfileForm_after_login() throws Exception {
        when(modelMapper.map(any(), any())).thenReturn(new ProfileForm());
        when(profileFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(get("/settings/profile"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profileForm"));
    }

    @DisplayName("프로필 변경 테스트 - 로그인 이전")
    @Test
    void updateProfile_before_login() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("프로필 변경 테스트 - 로그인 이후")
    @Test
    void updateProfile_after_login() throws Exception {
        when(profileFormValidator.supports(any())).thenReturn(true);

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

    @DisplayName("비밀번호 변경 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updatePasswordForm_before_login() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비밀번호 변경 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updatePasswordForm_after_login() throws Exception {
        when(passwordFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(get("/settings/password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @DisplayName("비밀번호 변경 테스트 - 로그인 이전")
    @Test
    void updatePassword_before_login() throws Exception {
        mockMvc.perform(post("/settings/password")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("비밀번호 변경 테스트 - 로그인 이후")
    @Test
    void updatePassword_after_login() throws Exception {
        when(passwordFormValidator.supports(any())).thenReturn(true);

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

    @DisplayName("알림 변경 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void updateNotificationForm_before_login() throws Exception {
        mockMvc.perform(get("/settings/notification"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("알림 변경 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void updateNotificationForm_after_login() throws Exception {
        when(modelMapper.map(any(), any())).thenReturn(new NotificationForm());

        mockMvc.perform(get("/settings/notification"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/notification"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notificationForm"));
    }

    @DisplayName("알림 변경 테스트 - 로그인 이전")
    @Test
    void updateNotification_before_login() throws Exception {
        mockMvc.perform(post("/settings/notification")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("알림 변경 테스트 - 로그인 이후")
    @Test
    void updateNotification_after_login() throws Exception {
        mockMvc.perform(post("/settings/notification")
                .param("commentNotification", "false")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notification"))
                .andExpect(flash().attributeExists("message"));

        verify(accountService, times(1)).updateNotification(any(), any());
    }

    @DisplayName("계정 삭제 페이지 보이는지 테스트 - 로그인 이전")
    @Test
    void deleteAccountForm_before_login() throws Exception {
        mockMvc.perform(get("/settings/account"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("계정 삭제 페이지 보이는지 테스트 - 로그인 이후")
    @Test
    void deleteAccountForm_after_login() throws Exception {
        mockMvc.perform(get("/settings/account"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("settings/account"))
                .andExpect(model().attributeExists("account"));
    }

    @DisplayName("계정 삭제 테스트 - 로그인 이전")
    @Test
    void deleteAccount_before_login() throws Exception {
        mockMvc.perform(delete("/settings/account")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("계정 삭제 테스트 - 로그인 이후")
    @Test
    void deleteAccount_after_login() throws Exception {
        mockMvc.perform(delete("/settings/account")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("alertType"))
                .andExpect(flash().attributeExists("message"));

        verify(accountService, times(1)).deleteAccount(any());
    }
}