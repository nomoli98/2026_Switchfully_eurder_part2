package com.application.eurder2.dtos.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateEurderRequest(
        @NotEmpty List<CreateItemGroupRequest> itemGroupRequests
) {
}
