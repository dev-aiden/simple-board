package com.aiden.dev.simpleboard.modules.notification;

import com.aiden.dev.simpleboard.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne
    private Account account;

    @CreationTimestamp
    private LocalDateTime createdAt;
}