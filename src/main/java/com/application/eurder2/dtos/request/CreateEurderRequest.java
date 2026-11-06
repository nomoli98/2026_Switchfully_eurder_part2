package com.application.eurder2.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateEurderRequest(
        @NotEmpty List<@NotNull @Valid CreateItemGroupRequest> itemGroupRequests
) {
}
