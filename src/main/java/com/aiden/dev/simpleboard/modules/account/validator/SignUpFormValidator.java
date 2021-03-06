package com.aiden.dev.simpleboard.modules.account.validator;

import com.aiden.dev.simpleboard.modules.account.AccountRepository;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        
        if(accountRepository.existsByLoginId(signUpForm.getLoginId())) {
            errors.rejectValue("loginId", "invalid.loginId", new Object[]{signUpForm.getLoginId()}, "이미 사용중인 아이디입니다.");
        }
        
        if(accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
        
        if(accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }
        
        if(!signUpForm.getPassword().equals(signUpForm.getPasswordConfirm())) {
            errors.rejectValue("password", "wrong.value", "입력한 비밀번호가 일치하지 않습니다.");
        }
    }
}
