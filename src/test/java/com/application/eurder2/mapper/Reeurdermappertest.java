package com.application.eurder2.mapper;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.request.CreateEurderRequest;
import com.application.eurder2.dtos.request.CreateItemGroupRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Unit tests for the mappers used by re-eurder: EurderMapper.toCreateRequest and ItemGroupMapper.toRequest. */
class ReEurderMapperTest {

    private Item itemWithId(UUID id, double price) {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(id);
        when(item.getPrice()).thenReturn(price);
        return item;
    }

    @Test
    void itemGroupToRequest_copiesItemIdAndAmount() {
        UUID penId = UUID.randomUUID();
        ItemGroup group = new ItemGroup(itemWithId(penId, 2.5), 4);

        CreateItemGroupRequest request = ItemGroupMapper.toRequest(group);

        assertThat(request).isEqualTo(new CreateItemGroupRequest(penId, 4));
    }

    @Test
    void eurderToCreateRequest_hasOneRequestPerGroup_inSameOrder() {
        UUID penId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Eurder eurder = new Eurder(List.of(
                new ItemGroup(itemWithId(penId, 2.5), 2),
                new ItemGroup(itemWithId(bookId, 15.0), 1)), mock(User.class));

        CreateEurderRequest request = EurderMapper.toCreateRequest(eurder);

        assertThat(request.itemGroupRequests()).containsExactly(
                new CreateItemGroupRequest(penId, 2),
                new CreateItemGroupRequest(bookId, 1));
    }
}