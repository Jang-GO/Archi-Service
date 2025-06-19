package com.archiservice.badword;

import com.archiservice.badword.service.BadWordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BadWordValidator.class)
@Documented
public @interface NoBadWords {
    String message() default "부적절한 언어가 포함되어 있습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

