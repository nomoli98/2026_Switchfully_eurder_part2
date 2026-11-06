package com.application.eurder2.service;

import com.application.eurder2.dtos.request.CreateItemGroupRequest;
import com.application.eurder2.dtos.response.ItemGroupResponse;
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

    public ItemGroupResponse createItemGroup(CreateItemGroupRequest request) {
        return ItemGroupMapper.toResponse(itemGroupRepository.save(ItemGroupMapper.toEntity(
                itemService.getItemById(request.itemId()),
                request.amount())));
    }
}
