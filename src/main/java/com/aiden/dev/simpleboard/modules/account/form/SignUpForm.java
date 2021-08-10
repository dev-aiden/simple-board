package com.aiden.dev.simpleboard.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {

    @NotBlank
    @Length(min = 3, max = 10)
    @Pattern(regexp = "^[a-z0-9_-]{3,10}$", message = "영어 소문자, 숫자만 입력가능합니다!")
    private String loginId;

    @NotBlank
    @Length(min = 8, max = 20)
    private String password;

    @NotBlank
    @Length(min = 8, max = 20)
    private String passwordConfirm;

    @NotBlank
    @Length(min = 3, max = 10)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{3,10}$", message = "영어, 한글, 숫자만 입력가능합니다!")
    private String nickname;

    @NotBlank
    @Email
    private String email;
}
