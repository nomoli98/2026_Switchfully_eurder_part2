package com.application.eurder2.validator.annotation;

import com.application.eurder2.validator.StrongPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "   String message() default \"Password needs 8+ characters, a digit, an uppercase letter and a special character\";";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}