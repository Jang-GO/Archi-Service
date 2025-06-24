package com.archiservice.badword.service;

import com.archiservice.badword.NoBadWords;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BadWordValidator implements ConstraintValidator<NoBadWords, String> {

    private final BadWordFilterService badWordFilterService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        boolean isValid = !badWordFilterService.containsBadWord(value);

        if (!isValid) {
            List<String> foundBadWords = badWordFilterService.findBadWords(value);
            String customMessage = "부적절한 언어가 감지되었습니다.";

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(customMessage)
                    .addConstraintViolation();
        }

        return isValid;
    }
}


