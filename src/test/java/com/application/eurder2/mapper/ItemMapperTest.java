package com.application.eurder2.mapper;

import com.application.eurder2.domain.Item;
import com.application.eurder2.dtos.request.CreateItemRequest;
import com.application.eurder2.dtos.response.ItemResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toEntity_copiesAllFields_andLeavesIdEmpty() {
        Item item = ItemMapper.toEntity(new CreateItemRequest("Laptop", "A fast laptop", 999.99, 10));

        assertThat(item.getId()).isNull();
        assertThat(item.getName()).isEqualTo("Laptop");
        assertThat(item.getDescription()).isEqualTo("A fast laptop");
        assertThat(item.getPrice()).isEqualTo(999.99);
        assertThat(item.getStock()).isEqualTo(10);
    }

    @Test
    void toResponse_copiesAllFields() {
        Item item = new Item("Laptop", "A fast laptop", 999.99, 10);

        ItemResponse response = ItemMapper.toResponse(item);

        assertThat(response.id()).isEqualTo(item.getId());
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.description()).isEqualTo("A fast laptop");
        assertThat(response.price()).isEqualTo(999.99);
        assertThat(response.stock()).isEqualTo(10);
    }

    @Test
    void toResponse_ofZeroValues() {
        ItemResponse response = ItemMapper.toResponse(new Item("Free", "Free sample", 0, 0));

        assertThat(response.price()).isEqualTo(0.0);
        assertThat(response.stock()).isEqualTo(0);
    }
}