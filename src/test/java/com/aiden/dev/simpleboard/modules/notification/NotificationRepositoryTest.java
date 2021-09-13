package com.aiden.dev.simpleboard.modules.notification;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.post.Post;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired AccountRepository accountRepository;
    @Autowired NotificationRepository notificationRepository;

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

    @DisplayName("Notification 개수 조회 쿼리 테스트")
    @Test
    void countByAccountAndChecked() {
        Account account = accountRepository.findByNickname("test");

        Notification notification = new Notification();
        notification.setChecked(false);
        notification.setAccount(account);
        notificationRepository.save(notification);

        assertThat(notificationRepository.countByAccountAndChecked(account, true)).isEqualTo(0L);
        assertThat(notificationRepository.countByAccountAndChecked(account, false)).isEqualTo(1L);
    }

    @DisplayName("Notification 조회 쿼리 테스트")
    @Test
    void findByAccountAndCheckedOrderByCreatedAtDesc() {
        Account account = accountRepository.findByNickname("test");

        Notification notification = new Notification();
        notification.setChecked(false);
        notification.setAccount(account);
        notificationRepository.save(notification);

        assertThat(notificationRepository.findByAccountAndCheckedOrderByCreatedAtDesc(account, true).size()).isEqualTo(0L);
        assertThat(notificationRepository.findByAccountAndCheckedOrderByCreatedAtDesc(account, false).size()).isEqualTo(1L);
    }

    @DisplayName("Notification 삭제 쿼리 테스트")
    @Test
    void deleteByAccountAndChecked() {
        Account account = accountRepository.findByNickname("test");

        Notification notification = new Notification();
        notification.setChecked(false);
        notification.setAccount(account);
        notificationRepository.save(notification);

        assertThat(notificationRepository.countByAccountAndChecked(account, false)).isEqualTo(1L);
        notificationRepository.deleteByAccountAndChecked(account, false);
        assertThat(notificationRepository.countByAccountAndChecked(account, false)).isEqualTo(0L);
    }
}