package com.aiden.dev.simpleboard.modules.account.form;

import com.aiden.dev.simpleboard.modules.account.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class Profile {

    @Length(min = 3, max = 10)
    private String nickname;

    public Profile(Account account) {
        this.nickname = account.getNickname();
    }
}
