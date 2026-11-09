package com.application.eurder2.service;

import com.application.eurder2.domain.Item;
import com.application.eurder2.dtos.request.CreateItemRequest;
import com.application.eurder2.dtos.request.UpdateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.mapper.ItemMapper;
import com.application.eurder2.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.function.Consumer;

@Service
public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {this.repository = repository;}

    public ItemResponse createItem(CreateItemRequest request) {
        return ItemMapper.toResponse(repository.save(ItemMapper.toEntity(request)));
    }

    public Item getItemById(UUID id) {
        return repository.findById(id)
                         .orElseThrow(() -> ExistenceException.notInDatabase("item"));
    }

    @Transactional
    public ItemResponse updateItem(UpdateItemRequest request, UUID itemId) {
        Item item = getItemById(itemId);
        updateIfHasText(request.name(), item::setName);
        updateIfHasText(request.description(), item::setDescription);
        updateIfPresent(request.price(), item::setPrice);
        updateIfPresent(request.stock(), item::setStock);
        return ItemMapper.toResponse(repository.save(item));
    }

    private static <T> void updateIfPresent(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }

    private static void updateIfHasText(String value, Consumer<String> setter) {
        if (StringUtils.hasText(value)) setter.accept(value);
    }


}
