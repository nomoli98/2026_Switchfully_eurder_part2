package com.application.eurder2.service;

import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.dtos.request.CreateItemGroupRequest;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.mapper.ItemGroupMapper;
import com.application.eurder2.repository.ItemGroupRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemGroupService {

    private final ItemGroupRepository itemGroupRepository;
    private final ItemService itemService;

    public ItemGroupService(ItemGroupRepository itemGroupRepository, ItemService itemService) {
        this.itemGroupRepository = itemGroupRepository;
        this.itemService = itemService;
    }

    public ItemGroup createItemGroup(CreateItemGroupRequest request) {
        if (!itemService.existsById(request.itemId()))
            throw ExistenceException.notInDatabase("item");
        return ItemGroupMapper.toEntity(itemService.getItemById(request.itemId()),
                                        request.amount());
    }
}
