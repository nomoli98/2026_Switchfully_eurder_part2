package com.application.eurder2.mapper;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.response.EurderResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EurderMapperTest {

    private ItemGroup pen() {
        return new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4);
    }

    private ItemGroup book() {
        return new ItemGroup(new Item("Book", "Thick book", 15.0, 10), 2);
    }

    @Test
    void toEntity_setsUserAndGroups_andLeavesIdEmpty() {
        User user = mock(User.class);
        ItemGroup pen = pen();

        Eurder eurder = EurderMapper.toEntity(List.of(pen), user);

        assertThat(eurder.getId()).isNull();
        assertThat(eurder.getUser()).isSameAs(user);
        assertThat(eurder.getItemGroups()).containsExactly(pen);
        assertThat(pen.getEurder()).isSameAs(eurder);
    }

    @Test
    void toResponse_copiesAllFields() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        Eurder eurder = new Eurder(List.of(pen(), book()), user);

        EurderResponse response = EurderMapper.toResponse(eurder);

        assertThat(response.eurderId()).isEqualTo(eurder.getId());
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.eurderPrice()).isEqualTo(40.0);
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).itemName()).isEqualTo("Pen");
        assertThat(response.items().get(0).groupPrice()).isEqualTo(10.0);
        assertThat(response.items().get(1).itemName()).isEqualTo("Book");
        assertThat(response.items().get(1).groupPrice()).isEqualTo(30.0);
    }
}