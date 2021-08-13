package com.aiden.dev.simpleboard.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class FindPasswordForm {

    @NotBlank
    @Length(min = 3, max = 10)
    @Pattern(regexp = "^[a-z0-9_-]{3,10}$", message = "영어 소문자, 숫자만 입력가능합니다!")
    private String loginId;

    @NotBlank
    @Email
    private String email;
}
