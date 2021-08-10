package com.aiden.dev.simpleboard.modules.account.form;

import com.aiden.dev.simpleboard.modules.account.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
public class ProfileForm {

    @NotBlank
    @Length(min = 3, max = 10)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{3,10}$", message = "영어, 한글, 숫자만 입력가능합니다!")
    private String nickname;

    private String profileImage;

    public ProfileForm(Account account) {
        this.nickname = account.getNickname();
        this.profileImage = account.getProfileImage();
    }
}
