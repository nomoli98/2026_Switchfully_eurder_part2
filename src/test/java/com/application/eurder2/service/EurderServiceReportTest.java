package com.application.eurder2.service;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.response.report.ReportResponse;
import com.application.eurder2.repository.EurderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for EurderService.getMyEurderReport. */
class EurderServiceReportTest {

    private EurderRepository repository;
    private EurderService service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(EurderRepository.class);
        service = new EurderService(repository, mock(ItemGroupService.class));
        user = mock(User.class);
    }

    @Test
    void getMyEurderReport_asksRepositoryForThisUserOnly() {
        when(repository.findByUserOrderByOrderDateDesc(user)).thenReturn(List.of());

        service.getMyEurderReport(user);

        verify(repository).findByUserOrderByOrderDateDesc(user);
    }

    @Test
    void getMyEurderReport_mapsEurdersOfUser() {
        Eurder pens = new Eurder(List.of(new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4)), user);     // 10.0
        Eurder books = new Eurder(List.of(new ItemGroup(new Item("Book", "Thick book", 15.0, 10), 2)), user); // 30.0
        when(repository.findByUserOrderByOrderDateDesc(user)).thenReturn(List.of(pens, books));

        ReportResponse report = service.getMyEurderReport(user);

        assertThat(report.eurders()).hasSize(2);
        assertThat(report.totalPrice()).isEqualTo(40.0);
    }

    @Test
    void getMyEurderReport_noEurders_returnsEmptyReport() {
        when(repository.findByUserOrderByOrderDateDesc(user)).thenReturn(List.of());

        ReportResponse report = service.getMyEurderReport(user);

        assertThat(report.eurders()).isEmpty();
        assertThat(report.totalPrice()).isZero();
    }
}