package com.application.eurder2.mapper;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.response.report.ReportItemGroupResponse;
import com.application.eurder2.dtos.response.report.ReportResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Unit tests for EurderMapper.toReport and ItemGroupMapper.toReport. */
class ReportMapperTest {

    private final User user = mock(User.class);

    private ItemGroup pen() {
        return new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4);     // 10.0
    }

    private ItemGroup book() {
        return new ItemGroup(new Item("Book", "Thick book", 15.0, 10), 2); // 30.0
    }

    @Test
    void itemGroupToReport_copiesNameAmountAndGroupPrice() {
        ReportItemGroupResponse response = ItemGroupMapper.toReport(pen());

        assertThat(response.itemName()).isEqualTo("Pen");
        assertThat(response.amount()).isEqualTo(4);
        assertThat(response.groupPrice()).isEqualTo(10.0);
    }

    @Test
    void itemGroupToReport_usesPriceAtOrder_notCurrentItemPrice() {
        ItemGroup group = pen();
        group.getItem().setPrice(100.0);

        assertThat(ItemGroupMapper.toReport(group).groupPrice()).isEqualTo(10.0);
    }

    @Test
    void toReport_oneEurder_copiesAllFields() {
        Eurder eurder = new Eurder(List.of(pen(), book()), user);

        ReportResponse report = EurderMapper.toReport(List.of(eurder));

        assertThat(report.eurders()).hasSize(1);
        assertThat(report.eurders().get(0).eurderId()).isEqualTo(eurder.getId());
        assertThat(report.eurders().get(0).itemGroups())
                .extracting(ReportItemGroupResponse::itemName)
                .containsExactly("Pen", "Book");
        assertThat(report.eurders().get(0).eurderPrice()).isEqualTo(40.0);
        assertThat(report.totalPrice()).isEqualTo(40.0);
    }

    @Test
    void toReport_severalEurders_totalIsSumOfEurders() {
        Eurder first = new Eurder(List.of(pen()), user);   // 10.0
        Eurder second = new Eurder(List.of(book()), user); // 30.0

        ReportResponse report = EurderMapper.toReport(List.of(first, second));

        assertThat(report.eurders()).hasSize(2);
        assertThat(report.totalPrice()).isEqualTo(40.0);
    }

    @Test
    void toReport_noEurders_emptyListAndZeroTotal() {
        ReportResponse report = EurderMapper.toReport(List.of());

        assertThat(report.eurders()).isEmpty();
        assertThat(report.totalPrice()).isZero();
    }
}