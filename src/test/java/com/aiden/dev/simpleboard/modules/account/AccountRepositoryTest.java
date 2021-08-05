package com.aiden.dev.simpleboard.modules.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {
        Account account = Account.builder()
                .loginId("test")
                .password("test")
                .nickname("test")
                .email("test@email.com")
                .build();
        account.generateEmailCheckToken();
        accountRepository.save(account);
    }

    @DisplayName("LoginID가 존재하는지 확인하는 쿼리 테스트")
    @Test
    void existsByLoginId() {
        assertThat(accountRepository.existsByLoginId("test")).isTrue();
        assertThat(accountRepository.existsByLoginId("test2")).isFalse();
    }

    @DisplayName("Nickname이 존재하는지 확인하는 쿼리 테스트")
    @Test
    void existsByNickname() {
        assertThat(accountRepository.existsByNickname("test")).isTrue();
        assertThat(accountRepository.existsByNickname("test2")).isFalse();
    }

    @DisplayName("Email이 존재하는지 확인하는 쿼리 테스트")
    @Test
    void existsByEmail() {
        assertThat(accountRepository.existsByEmail("test@email.com")).isTrue();
        assertThat(accountRepository.existsByEmail("test2@email.com")).isFalse();
    }
}