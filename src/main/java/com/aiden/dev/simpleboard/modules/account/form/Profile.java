package com.aiden.dev.simpleboard.modules.account.form;

import com.aiden.dev.simpleboard.modules.account.Account;
import lombok.Data;

@Data
public class Profile {

    private String nickname;

    public Profile(Account account) {
        this.nickname = account.getNickname();
    }
}
