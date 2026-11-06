package com.application.eurder2.dtos.request;

import com.application.eurder2.validator.annotation.PhoneNumber;
import com.application.eurder2.validator.annotation.StrongPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        @NotBlank String firstname,
        @NotBlank String lastname,
        @NotBlank @Email String email,
        @NotBlank @StrongPassword String password,
        @PhoneNumber String phoneNumber,
        @NotNull @Valid AddressRequest address) {
}
