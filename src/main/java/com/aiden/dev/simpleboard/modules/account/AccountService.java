package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.infra.mail.EmailMessage;
import com.aiden.dev.simpleboard.infra.mail.EmailService;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    public Account processNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("Simple Board 회원가입 인증")
                .message("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail())
                .build();
        emailService.sendEmail(emailMessage);
    }
}
