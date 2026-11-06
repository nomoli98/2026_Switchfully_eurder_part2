package com.application.eurder2.mapper;

import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.dtos.response.ItemGroupResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ItemGroupMapperTest {

    @Test
    void toEntity_usesItemAndAmount_andLeavesIdEmpty() {
        Item pen = new Item("Pen", "Blue pen", 2.5, 10);

        ItemGroup group = ItemGroupMapper.toEntity(pen, 4);

        assertThat(group.getId()).isNull();
        assertThat(group.getItem()).isSameAs(pen);
        assertThat(group.getAmount()).isEqualTo(4);
        assertThat(group.getPriceAtOrder()).isEqualTo(2.5);
    }

    @Test
    void toResponse_copiesAllFields() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4);

        ItemGroupResponse response = ItemGroupMapper.toResponse(group);

        assertThat(response).isEqualTo(
                new ItemGroupResponse("Pen", 4, 10.0, LocalDate.now().plusDays(1)));
    }
}