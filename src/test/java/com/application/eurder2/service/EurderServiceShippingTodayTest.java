package com.application.eurder2.service;

import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.dtos.response.shippingToday.EurderShippingTodayResponse;
import com.application.eurder2.dtos.response.shippingToday.ItemGroupShippingTodayResponse;
import com.application.eurder2.repository.EurderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for EurderService.getAllEurdersShippingToday. */
class EurderServiceShippingTodayTest {

    private EurderRepository repository;
    private EurderService service;

    @BeforeEach
    void setUp() {
        repository = mock(EurderRepository.class);
        service = new EurderService(repository, mock(ItemGroupService.class));
    }

    private ItemGroup group(String name, int amount, LocalDate shippingDate) {
        ItemGroup group = new ItemGroup(new Item(name, name + " description", 2.5, 10), amount);
        group.setShippingDate(shippingDate);
        return group;
    }

    @Test
    void asksRepositoryForToday() {
        when(repository.findAllWithItemGroupShippingOn(any())).thenReturn(List.of());

        service.getAllEurdersShippingToday();

        verify(repository).findAllWithItemGroupShippingOn(LocalDate.now());
    }

    @Test
    void mapsEurders_andKeepsOnlyGroupsShippingToday() {
        LocalDate today = LocalDate.now();
        Eurder eurder = new Eurder(List.of(group("Pen", 1, today),
                                           group("Book", 1, today.plusDays(7))), TestData.newUser());
        when(repository.findAllWithItemGroupShippingOn(any())).thenReturn(List.of(eurder));

        List<EurderShippingTodayResponse> result = service.getAllEurdersShippingToday();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).itemGroup())
                .extracting(ItemGroupShippingTodayResponse::itemName)
                .containsExactly("Pen");
    }

    @Test
    void nothingShipsToday_returnsEmptyList() {
        when(repository.findAllWithItemGroupShippingOn(any())).thenReturn(List.of());

        assertThat(service.getAllEurdersShippingToday()).isEmpty();
    }
}