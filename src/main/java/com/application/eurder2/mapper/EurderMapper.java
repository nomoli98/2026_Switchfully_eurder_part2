package com.application.eurder2.mapper;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.response.EurderResponse;

import java.util.List;

public class EurderMapper {
    public static Eurder toEntity(List<ItemGroup> items, User user) {
        return new Eurder(items, user);
    }

    public static EurderResponse toResponse(Eurder eurder) {
        return new EurderResponse(eurder.getId(),
                                  eurder.getItemGroups()
                                        .stream()
                                        .map(ItemGroupMapper::toResponse)
                                        .toList(),
                                  eurder.getTotalPrice(),
                                  eurder.getUser().getId());
    }
}
