package com.aiden.dev.simpleboard.modules.account;

import com.aiden.dev.simpleboard.infra.mail.EmailMessage;
import com.aiden.dev.simpleboard.infra.mail.EmailService;
import com.aiden.dev.simpleboard.modules.account.form.FindPasswordForm;
import com.aiden.dev.simpleboard.modules.account.form.NotificationForm;
import com.aiden.dev.simpleboard.modules.account.form.ProfileForm;
import com.aiden.dev.simpleboard.modules.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

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

    public void sendSignUpConfirmEmail(Account newAccount) {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("Simple Board 회원가입 인증")
                .message("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail())
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token= new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Account account = accountRepository.findByLoginId(loginId);
        if(account == null) {
            throw new UsernameNotFoundException(loginId);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, ProfileForm profileForm) {
        modelMapper.map(profileForm, account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotification(Account account, NotificationForm notificationForm) {
        modelMapper.map(notificationForm, account);
        accountRepository.save(account);
    }

    public void deleteAccount(Account account) {
        accountRepository.delete(account);
    }

    public void sendFindPasswordEmail(FindPasswordForm findPasswordForm) {
        Account account = accountRepository.findByLoginId(findPasswordForm.getLoginId());
        String plainPassword = UUID.randomUUID().toString();
        updatePassword(account, plainPassword);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(findPasswordForm.getEmail())
                .subject("Simple Board 임시 비밀번호 발급")
                .message("임시 비밀번호 : " + plainPassword + "\n로그인 후 반드시 비밀번호를 변경하세요!")
                .build();
        emailService.sendEmail(emailMessage);
    }
}
