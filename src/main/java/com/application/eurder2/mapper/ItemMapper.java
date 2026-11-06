package com.application.eurder2.mapper;

import com.application.eurder2.domain.Item;
import com.application.eurder2.dtos.request.CreateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;

public class ItemMapper {
    public static Item toEntity(CreateItemRequest request) {
        return new Item(request.name(), request.description(), request.price(), request.stock());
    }

    public static ItemResponse toResponse(Item item) {
        return new ItemResponse(item.getId(),
                                item.getName(),
                                item.getDescription(),
                                item.getPrice(),
                                item.getStock());
    }
}
