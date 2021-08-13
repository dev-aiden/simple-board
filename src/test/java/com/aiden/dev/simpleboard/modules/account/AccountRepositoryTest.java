package com.aiden.dev.simpleboard.modules.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired AccountRepository accountRepository;

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

    @DisplayName("LoginID로 계정 존재여부 확인하는 쿼리 테스트")
    @Test
    void existsByLoginId() {
        assertThat(accountRepository.existsByLoginId("test")).isTrue();
        assertThat(accountRepository.existsByLoginId("test2")).isFalse();
    }

    @DisplayName("Nickname으로 계정 존재여부 확인하는 쿼리 테스트")
    @Test
    void existsByNickname() {
        assertThat(accountRepository.existsByNickname("test")).isTrue();
        assertThat(accountRepository.existsByNickname("test2")).isFalse();
    }

    @DisplayName("Email로 계정 존재여부 확인하는 쿼리 테스트")
    @Test
    void existsByEmail() {
        assertThat(accountRepository.existsByEmail("test@email.com")).isTrue();
        assertThat(accountRepository.existsByEmail("test2@email.com")).isFalse();
    }

    @DisplayName("Email로 계정 조회하는 쿼리 테스트")
    @Test
    void findByEmail() {
        Account account = accountRepository.findByEmail("test@email.com");
        assertThat(account).isNotNull();
        assertThat(account.getLoginId()).isEqualTo("test");
        assertThat(account.getNickname()).isEqualTo("test");
        assertThat(accountRepository.findByEmail("test2@email.com")).isNull();
    }

    @DisplayName("LoginId로 계정 조회하는 쿼리 테스트")
    @Test
    void findByLoginId() {
        Account account = accountRepository.findByLoginId("test");
        assertThat(account).isNotNull();
        assertThat(account.getEmail()).isEqualTo("test@email.com");
        assertThat(account.getNickname()).isEqualTo("test");
        assertThat(accountRepository.findByLoginId("test2")).isNull();
    }

    @DisplayName("Nickname으로 계정 조회하는 쿼리 테스트")
    @Test
    void findByNickname() {
        Account account = accountRepository.findByNickname("test");
        assertThat(account).isNotNull();
        assertThat(account.getEmail()).isEqualTo("test@email.com");
        assertThat(account.getLoginId()).isEqualTo("test");
        assertThat(accountRepository.findByNickname("test2")).isNull();
    }

    @DisplayName("LoginId와 Email로 계정 존재여부 확인하는 쿼리 테스트")
    @Test
    void existsByLoginIdAndEmail() {
        assertThat(accountRepository.existsByLoginIdAndEmail("test", "test@email.com")).isTrue();
        assertThat(accountRepository.existsByLoginIdAndEmail("test2", "test@email.com")).isFalse();
        assertThat(accountRepository.existsByLoginIdAndEmail("test", "test2@email.com")).isFalse();
    }
}