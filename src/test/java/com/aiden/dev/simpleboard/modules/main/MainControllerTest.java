package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.account.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainControllerTest.class)
class MainControllerTest {

    @Autowired MockMvc mockMvc;
    
    @DisplayName("메인 페이지 보이는지 테스트 - Account 미존재")
    @Test
    void home_not_exist_account() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @DisplayName("메인 페이지 보이는지 테스트 - Account 존재")
    @Test
    void home_exist_account() throws Exception {
        mockMvc.perform(get("/")
                    .flashAttr("account", new Account()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("account"));
    }
}