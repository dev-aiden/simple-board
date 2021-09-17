package com.aiden.dev.simpleboard.modules.notification;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.WithAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean DataSource dataSource;
    @MockBean NotificationRepository notificationRepository;
    @MockBean NotificationService notificationService;

    @DisplayName("읽지 않은 알림 목록페이지 보이는지 확인 - 로그인 이전")
    @Test
    void getNotifications_before_login() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("읽지 않은 알림 목록페이지 보이는지 확인 - 로그인 이후")
    @Test
    void getNotifications_after_login() throws Exception {
        given(notificationRepository.findByAccountAndCheckedOrderByCreatedAtDesc(any(Account.class), anyBoolean())).willReturn(List.of(new Notification()));
        given(notificationRepository.countByAccountAndChecked(any(Account.class), anyBoolean())).willReturn(1L);

        mockMvc.perform(get("/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notification/list"))
                .andExpect(model().attributeExists("isNew"))
                .andExpect(model().attributeExists("numberOfNotChecked"))
                .andExpect(model().attributeExists("numberOfChecked"))
                .andExpect(model().attributeExists("notifications"));

        verify(notificationService, times(1)).markAsRead(any(List.class));
    }

    @DisplayName("읽은 알림 목록페이지 보이는지 확인 - 로그인 이전")
    @Test
    void getOldNotifications_before_login() throws Exception {
        mockMvc.perform(get("/notifications/old"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("읽은 알림 목록페이지 보이는지 확인 - 로그인 이후")
    @Test
    void getOldNotifications_after_login() throws Exception {
        given(notificationRepository.findByAccountAndCheckedOrderByCreatedAtDesc(any(Account.class), anyBoolean())).willReturn(List.of(new Notification()));
        given(notificationRepository.countByAccountAndChecked(any(Account.class), anyBoolean())).willReturn(1L);

        mockMvc.perform(get("/notifications/old"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notification/list"))
                .andExpect(model().attributeExists("isNew"))
                .andExpect(model().attributeExists("numberOfNotChecked"))
                .andExpect(model().attributeExists("numberOfChecked"))
                .andExpect(model().attributeExists("notifications"));
    }

    @DisplayName("알림 삭제 테스트 - 로그인 이전")
    @Test
    void deleteNotifications_before_login() throws Exception {
        mockMvc.perform(delete("/notifications")
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("알림 삭제 테스트 - 로그인 이후")
    @Test
    void deleteNotifications_after_login() throws Exception {
        mockMvc.perform(delete("/notifications")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService, times(1)).deleteNotification(any(Account.class));
    }
}