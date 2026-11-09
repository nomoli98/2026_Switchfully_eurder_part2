package com.application.eurder2.dtos.request;


import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
        String name,
        @Size(max = 255) String description,
        @PositiveOrZero Double price,
        @PositiveOrZero Integer stock
) {
}
