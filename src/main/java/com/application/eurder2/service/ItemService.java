package com.application.eurder2.service;

import com.application.eurder2.dtos.request.CreateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;
import com.application.eurder2.mapper.ItemMapper;
import com.application.eurder2.repository.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {this.repository = repository;}

    public ItemResponse createItem(CreateItemRequest request){
        return ItemMapper.toResponse(repository.save(ItemMapper.toEntity(request)));
    }

}
