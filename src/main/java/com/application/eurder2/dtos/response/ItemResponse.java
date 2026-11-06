package com.application.eurder2.dtos.response;

import java.util.UUID;

public record ItemResponse(
        UUID id,
        String name,
        String description,
        Double price,
        Integer stock
) {
}
