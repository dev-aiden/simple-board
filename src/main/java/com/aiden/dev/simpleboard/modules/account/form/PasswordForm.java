package com.aiden.dev.simpleboard.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordForm {

    @NotBlank
    @Length(min = 8, max = 20)
    private String newPassword;

    @NotBlank
    @Length(min = 8, max = 20)
    private String newPasswordConfirm;
}
