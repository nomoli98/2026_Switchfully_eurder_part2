package com.application.eurder2.service;

import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.dtos.request.CreateItemGroupRequest;
import com.application.eurder2.exception.ExistenceException;
import com.application.eurder2.repository.ItemGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemGroupServiceTest {

    private ItemService itemService;
    private ItemGroupService service;

    @BeforeEach
    void setUp() {
        itemService = mock(ItemService.class);
        service = new ItemGroupService(mock(ItemGroupRepository.class), itemService);
    }

    @Test
    void createItemGroup_usesItemAndAmount() {
        UUID id = UUID.randomUUID();
        Item pen = new Item("Pen", "Blue pen", 2.5, 10);
        when(itemService.getItemById(id)).thenReturn(pen);

        ItemGroup group = service.createItemGroup(new CreateItemGroupRequest(id, 4));

        assertThat(group.getItem()).isSameAs(pen);
        assertThat(group.getAmount()).isEqualTo(4);
        assertThat(group.getGroupPrice()).isEqualTo(10.0);
    }

    @Test
    void createItemGroup_decreasesStock() {
        UUID id = UUID.randomUUID();
        Item pen = new Item("Pen", "Blue pen", 2.5, 10);
        when(itemService.getItemById(id)).thenReturn(pen);

        service.createItemGroup(new CreateItemGroupRequest(id, 4));

        assertThat(pen.getStock()).isEqualTo(6);
    }

    @Test
    void createItemGroup_shippingDateUsesStockBeforeDecrease() {
        UUID id = UUID.randomUUID();
        Item pen = new Item("Pen", "Blue pen", 2.5, 10);
        when(itemService.getItemById(id)).thenReturn(pen);

        ItemGroup group = service.createItemGroup(new CreateItemGroupRequest(id, 10));

        assertThat(group.getShippingDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(pen.getStock()).isZero();
    }

    @Test
    void createItemGroup_unknownItem_throws() {
        UUID id = UUID.randomUUID();
        when(itemService.getItemById(id)).thenThrow(ExistenceException.notInDatabase("item"));

        assertThatThrownBy(() -> service.createItemGroup(new CreateItemGroupRequest(id, 1)))
                .isInstanceOf(ExistenceException.class);
    }
}