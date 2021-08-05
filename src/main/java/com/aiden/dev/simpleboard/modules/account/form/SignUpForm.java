package com.aiden.dev.simpleboard.modules.account.form;

import lombok.Data;

@Data
public class SignUpForm {

    private String loginId;

    private String password;

    private String passwordConfirm;

    private String nickname;

    private String email;
}
