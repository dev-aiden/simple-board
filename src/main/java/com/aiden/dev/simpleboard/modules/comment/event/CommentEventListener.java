package com.aiden.dev.simpleboard.modules.comment.event;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.comment.Comment;
import com.aiden.dev.simpleboard.modules.notification.Notification;
import com.aiden.dev.simpleboard.modules.notification.NotificationRepository;
import com.aiden.dev.simpleboard.modules.post.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Async
@Transactional
@Component
@RequiredArgsConstructor
public class CommentEventListener {

    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleNewCommentEvent(NewCommentEvent newCommentEvent) {
        Comment comment = newCommentEvent.getComment();
        Account account = comment.getAccount();
        if(account.isCommentNotification()) {
            saveNewCommentNotification(comment.getPost(), account);
        }
    }

    private void saveNewCommentNotification(Post post, Account account) {
        Notification notification = new Notification();
        notification.setLink("/post/" + post.getId());
        notification.setMessage("'" + post.getTitle() + "'에 댓글이 추가되었습니다.");
        notification.setChecked(false);
        notification.setAccount(account);
        notificationRepository.save(notification);
    }
}
