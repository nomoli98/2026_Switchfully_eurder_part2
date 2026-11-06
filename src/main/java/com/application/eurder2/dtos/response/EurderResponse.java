package com.application.eurder2.dtos.response;

import java.util.List;
import java.util.UUID;

public record EurderResponse (
        UUID eurderId,
        List<ItemGroupResponse> items,
        double eurderPrice,
        UUID userId
) {}
