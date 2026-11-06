package com.application.eurder2.service;

import com.application.eurder2.domain.Item;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemServiceGetItemByIdTest {

    private ItemRepository repository;
    private ItemService service;

    @BeforeEach
    void setUp() {
        repository = mock(ItemRepository.class);
        service = new ItemService(repository);
    }

    @Test
    void knownId_returnsItem() {
        UUID id = UUID.randomUUID();
        Item pen = new Item("Pen", "Blue pen", 2.5, 10);
        when(repository.findById(id)).thenReturn(Optional.of(pen));

        assertThat(service.getItemById(id)).isSameAs(pen);
    }

    @Test
    void unknownId_throwsExistenceException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getItemById(id))
                .isInstanceOf(ExistenceException.class)
                .hasMessage("This item is not found in the database.");
    }
}