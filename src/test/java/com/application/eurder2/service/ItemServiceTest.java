package com.application.eurder2.service;

import com.application.eurder2.domain.Item;
import com.application.eurder2.dtos.request.CreateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;
import com.application.eurder2.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    private ItemRepository repository;
    private ItemService service;

    @BeforeEach
    void setUp() {
        repository = mock(ItemRepository.class);
        service = new ItemService(repository);
    }

    @Test
    void createItem_savesEntityWithRequestValues() {
        when(repository.save(any(Item.class))).thenAnswer(call -> call.getArgument(0));

        service.createItem(new CreateItemRequest("Laptop", "A fast laptop", 999.99, 10));

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(repository, times(1)).save(captor.capture());
        Item saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Laptop");
        assertThat(saved.getDescription()).isEqualTo("A fast laptop");
        assertThat(saved.getPrice()).isEqualTo(999.99);
        assertThat(saved.getStock()).isEqualTo(10);
    }

    @Test
    void createItem_returnsResponseOfSavedItem() {
        UUID id = UUID.randomUUID();
        Item saved = mock(Item.class);
        when(saved.getId()).thenReturn(id);
        when(saved.getName()).thenReturn("Laptop");
        when(saved.getDescription()).thenReturn("A fast laptop");
        when(saved.getPrice()).thenReturn(999.99);
        when(saved.getStock()).thenReturn(10);
        when(repository.save(any(Item.class))).thenReturn(saved);

        ItemResponse response = service.createItem(new CreateItemRequest("Laptop", "A fast laptop", 999.99, 10));

        assertThat(response).isEqualTo(new ItemResponse(id, "Laptop", "A fast laptop", 999.99, 10));
    }
}