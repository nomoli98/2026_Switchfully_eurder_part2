package com.application.eurder2.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String street,
        @NotBlank String number,
        @NotBlank String city,
        @NotBlank String postalCode,
        @NotBlank String country
) {
}
