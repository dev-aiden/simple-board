package com.aiden.dev.simpleboard.modules.account;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFactory.class)
public @interface WithAccount {

    String loginId() default "test";

    String role() default "ROLE_USER";

    boolean isEmailVerified() default false;

    long minusHoursForEmailCheckToken() default 0L;
}