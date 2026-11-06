package com.application.eurder2.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateItemGroupRequest(
        @NotBlank UUID itemId,
        @NotNull @PositiveOrZero int amount
) {
}
