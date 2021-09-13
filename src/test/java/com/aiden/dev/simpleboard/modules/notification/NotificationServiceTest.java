package com.aiden.dev.simpleboard.modules.notification;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks NotificationService notificationService;
    @Mock NotificationRepository notificationRepository;

    @DisplayName("읽음 처리 테스트")
    @Test
    void markAsRead() {
        // Given
        Account account = Account.builder()
                .loginId("test")
                .password("test")
                .nickname("test")
                .email("test@email.com")
                .build();
        account.generateEmailCheckToken();

        Notification notification = new Notification();
        notification.setChecked(false);
        notification.setAccount(account);

        // When
        notificationService.markAsRead(List.of(notification));

        // Then
        verify(notificationRepository).saveAll(any(List.class));
    }
}