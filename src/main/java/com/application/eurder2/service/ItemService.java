package com.application.eurder2.service;

import com.application.eurder2.domain.Item;
import com.application.eurder2.dtos.request.CreateItemRequest;
import com.application.eurder2.dtos.request.UpdateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;
import com.application.eurder2.enums.StockIndicator;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.mapper.ItemMapper;
import com.application.eurder2.repository.ItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import jakarta.persistence.criteria.Predicate;

@Service
public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {this.repository = repository;}

    public ItemResponse createItem(CreateItemRequest request) {
        return ItemMapper.toResponse(repository.save(ItemMapper.toEntity(request)));
    }

    public Item getItemById(UUID id) {
        return repository.findById(id).orElseThrow(() -> ExistenceException.notInDatabase("item"));
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

    public List<ItemResponse> findItems(String name,
                                        StockIndicator stockIndicator,
                                        Sort.Direction direction,
                                        Integer limit) {
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                                       "%" + name.toLowerCase() + "%"));
            }

            if (stockIndicator != null) {
                // WHERE stock >= lower AND stock < upper
                predicates.add(cb.greaterThanOrEqualTo(root.get("stock"), stockIndicator.getLowerBound()));
                predicates.add(cb.lessThan(root.get("stock"), stockIndicator.getUpperBound()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(direction != null ? direction : Sort.Direction.ASC, "name");

        List<Item> items = (limit == null)
                           ? repository.findAll(spec, sort)
                           : repository.findAll(spec, PageRequest.of(0, limit, sort)).getContent();

        return items.stream().map(ItemMapper::toResponse).toList();
    }


}
