package com.aiden.dev.simpleboard.modules.post;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.account.UserAccount;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String contents;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @Column(columnDefinition = "bigint default 0")
    private Long hits;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isWriter(UserAccount userAccount) {
        return this.getAccount().getLoginId().equals(userAccount.getUsername());
    }

    public boolean isAuthenticated(Account account) {
        if(this.postType == PostType.PRIVATE) {
            return account != null && Objects.equals(this.account.getLoginId(), account.getLoginId());
        }
        return true;
    }
}
