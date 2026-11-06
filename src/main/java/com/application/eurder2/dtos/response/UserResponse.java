package com.application.eurder2.dtos.response;

import com.application.eurder2.enums.Role;

import java.util.UUID;

public record UserResponse(UUID firstname,
                           String lastname,
                           String email,
                           String password,
                           String phoneNumber,
                           AddressResponse address,
                           Role role
) {
}
