package com.archiservice.common;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserFactory.class)
public @interface WithMockCustomUser {
    long id() default 1L;

    String email() default "test@test.com";

    String password() default "password";

    long tagCode() default 1000L;

    String ageCode() default "20";

    String[] authorities() default {};
}
