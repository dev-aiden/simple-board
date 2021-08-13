package com.aiden.dev.simpleboard.modules.account.validator;

import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.account.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ProfileFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(ProfileForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProfileForm profileForm = (ProfileForm) target;

        if(accountRepository.existsByNickname(profileForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{profileForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
    }
}
