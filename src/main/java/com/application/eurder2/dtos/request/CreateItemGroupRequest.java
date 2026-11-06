package com.application.eurder2.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateItemGroupRequest(
        @NotNull UUID itemId,
        @NotNull @Positive Integer amount
) {
}
