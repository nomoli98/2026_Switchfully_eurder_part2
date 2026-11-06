package com.application.eurder2.validator;

import com.application.eurder2.validator.annotation.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{};:'\",.<>/?\\|`~";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.length() >= 8
               && value.chars().anyMatch(Character::isDigit)
               && value.chars().anyMatch(Character::isUpperCase)
               && value.chars().anyMatch(c -> SPECIAL.indexOf(c) >= 0);
    }
}