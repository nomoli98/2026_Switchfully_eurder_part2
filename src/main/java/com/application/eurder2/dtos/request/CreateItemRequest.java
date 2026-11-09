package com.application.eurder2.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
        @NotBlank @Size(max = 60) String name,
        @NotBlank @Size(max = 255) String description,
        @NotNull @PositiveOrZero Double price,
        @NotNull @PositiveOrZero Integer stock
) {
}
