package com.aiden.dev.simpleboard.modules.comment;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.post.Post;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode
@Builder @AllArgsConstructor @NoArgsConstructor
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Post post;

    @Lob
    private String contents;

    @Enumerated(EnumType.STRING)
    private CommentType commentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Account account;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
