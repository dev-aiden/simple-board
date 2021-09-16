package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.account.AccountService;
import com.aiden.dev.simpleboard.modules.account.WithAccount;
import com.aiden.dev.simpleboard.modules.notification.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
class MainControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AccountService accountService;
    @MockBean PostService postService;
    @MockBean DataSource dataSource;
    @MockBean NotificationRepository notificationRepository;

    @DisplayName("메인 페이지 보이는지 테스트 - Account 미존재")
    @Test
    void home_not_exist_account() throws Exception {
        given(postService.getPosts(any(), anyString(), anyString()))
                .willReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("posts"));
    }

    @WithAccount(loginId = "aiden")
    @DisplayName("메인 페이지 보이는지 테스트 - Account 존재")
    @Test
    void home_exist_account() throws Exception {
        given(postService.getPosts(any(), anyString(), anyString()))
                .willReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("posts"));
    }

    @DisplayName("로그인 페이지 보이는지 테스트")
    @Test
    void login() throws Exception {
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}