package com.aiden.dev.simpleboard.modules.account.validator;

import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.account.form.FindPasswordForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class FindPasswordFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(FindPasswordForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        FindPasswordForm findPasswordForm = (FindPasswordForm) target;

        if(!accountRepository.existsByLoginIdAndEmail(findPasswordForm.getLoginId(), findPasswordForm.getEmail())) {
            errors.rejectValue("loginId", "invalid.loginId", new Object[]{findPasswordForm.getLoginId()}, "아이디와 이메일이 일치하는 사용자를 찾을 수 없습니다.");
        }
    }
}
