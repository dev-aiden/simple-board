package com.aiden.dev.simpleboard.modules.account;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.LocalDateTime;
import java.util.List;

public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    @Override
    public SecurityContext createSecurityContext(WithAccount withAccount) {
        Account account = Account.builder()
                .loginId(withAccount.loginId())
                .password("12345678")
                .nickname(withAccount.loginId())
                .email(withAccount.loginId() + "@email.com")
                .emailCheckTokenGeneratedAt(LocalDateTime.now().minusHours(withAccount.minusHoursForEmailCheckToken()))
                .build();

        UsernamePasswordAuthenticationToken token= new UsernamePasswordAuthenticationToken(new UserAccount(account), account.getPassword(), List.of(new SimpleGrantedAuthority(withAccount.role())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);

        return context;
    }
}