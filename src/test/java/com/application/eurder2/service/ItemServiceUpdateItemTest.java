package com.application.eurder2.service;

import com.application.eurder2.domain.Item;
import com.application.eurder2.dtos.request.UpdateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceUpdateItemTest {

    private ItemRepository repository;
    private ItemService service;
    private UUID id;
    private Item pen;

    @BeforeEach
    void setUp() {
        repository = mock(ItemRepository.class);
        service = new ItemService(repository);
        id = UUID.randomUUID();
        pen = new Item("Pen", "Blue pen", 2.5, 10);
        when(repository.findById(id)).thenReturn(Optional.of(pen));
        when(repository.save(any(Item.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void allFields_updatesAllFields() {
        service.updateItem(new UpdateItemRequest("Pencil", "Black pencil", 1.0, 5), id);

        assertThat(pen.getName()).isEqualTo("Pencil");
        assertThat(pen.getDescription()).isEqualTo("Black pencil");
        assertThat(pen.getPrice()).isEqualTo(1.0);
        assertThat(pen.getStock()).isEqualTo(5);
    }

    @Test
    void onlyName_changesOnlyName() {
        service.updateItem(new UpdateItemRequest("Pencil", null, null, null), id);

        assertThat(pen.getName()).isEqualTo("Pencil");
        assertThat(pen.getDescription()).isEqualTo("Blue pen");
        assertThat(pen.getPrice()).isEqualTo(2.5);
        assertThat(pen.getStock()).isEqualTo(10);
    }

    @Test
    void allNull_changesNothing() {
        service.updateItem(new UpdateItemRequest(null, null, null, null), id);

        assertThat(pen.getName()).isEqualTo("Pen");
        assertThat(pen.getDescription()).isEqualTo("Blue pen");
        assertThat(pen.getPrice()).isEqualTo(2.5);
        assertThat(pen.getStock()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankText_isIgnored(String blank) {
        service.updateItem(new UpdateItemRequest(blank, blank, null, null), id);

        assertThat(pen.getName()).isEqualTo("Pen");
        assertThat(pen.getDescription()).isEqualTo("Blue pen");
    }

    @Test
    void zeroPriceAndStock_areApplied() {
        service.updateItem(new UpdateItemRequest(null, null, 0.0, 0), id);

        assertThat(pen.getPrice()).isEqualTo(0.0);
        assertThat(pen.getStock()).isEqualTo(0);
    }

    @Test
    void savesItemAndReturnsResponse() {
        ItemResponse response = service.updateItem(new UpdateItemRequest("Pencil", null, null, null), id);

        verify(repository, times(1)).save(pen);
        assertThat(response).isEqualTo(new ItemResponse(null, "Pencil", "Blue pen", 2.5, 10));
    }

    @Test
    void unknownId_throwsExistenceException_andSavesNothing() {
        UUID unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateItem(new UpdateItemRequest("Pencil", null, null, null), unknown))
                .isInstanceOf(ExistenceException.class)
                .hasMessage("This item is not found in the database.");
        verify(repository, never()).save(any());
    }
}