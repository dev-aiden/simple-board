package com.aiden.dev.simpleboard.modules.notification;

import com.aiden.dev.simpleboard.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void markAsRead(List<Notification> notifications) {
        notifications.forEach(n -> n.setChecked(true));
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
    }
}
