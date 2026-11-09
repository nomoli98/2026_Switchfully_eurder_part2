package com.application.eurder2.mapper;

import com.application.eurder2.data.TestData;
import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import com.application.eurder2.dtos.response.AddressResponse;
import com.application.eurder2.dtos.response.shippingToday.EurderShippingTodayResponse;
import com.application.eurder2.dtos.response.shippingToday.ItemGroupShippingTodayResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for EurderMapper.toListShippingTodayResponse and ItemGroupMapper.toItemGroupShippingToday. */
class ShippingTodayMapperTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 9);

    private final User user = TestData.newUser(); // address: Main Street 12A, 1000 Brussels, Belgium

    private ItemGroup group(String name, double price, int amount, LocalDate shippingDate) {
        ItemGroup group = new ItemGroup(new Item(name, name + " description", price, 10), amount);
        group.setShippingDate(shippingDate);
        return group;
    }

    @Test
    void itemGroupToShippingToday_copiesAmountNameAndGroupPrice() {
        ItemGroupShippingTodayResponse response =
                ItemGroupMapper.toItemGroupShippingToday(group("Pen", 2.5, 4, TODAY));

        assertThat(response).isEqualTo(new ItemGroupShippingTodayResponse(4, "Pen", 10.0));
    }

    @Test
    void itemGroupToShippingToday_usesPriceAtOrder_notCurrentItemPrice() {
        ItemGroup pen = group("Pen", 2.5, 4, TODAY);
        pen.getItem().setPrice(100.0);

        assertThat(ItemGroupMapper.toItemGroupShippingToday(pen).groupPrice()).isEqualTo(10.0);
    }

    @Test
    void toList_mapsShippingAddressOfEurderUser() {
        Eurder eurder = new Eurder(List.of(group("Pen", 2.5, 1, TODAY)), user);

        List<EurderShippingTodayResponse> result = EurderMapper.toListShippingTodayResponse(List.of(eurder), TODAY);

        assertThat(result).singleElement()
                          .extracting(EurderShippingTodayResponse::shippingAddress)
                          .isEqualTo(new AddressResponse("Main Street", "12A", "Brussels", "1000", "Belgium"));
    }

    @Test
    void toList_keepsOnlyItemGroupsShippingOnDate() {
        Eurder eurder = new Eurder(List.of(group("Pen", 2.5, 1, TODAY),
                                           group("Book", 15.0, 1, TODAY.plusDays(7)),
                                           group("Lamp", 30.0, 2, TODAY)), user);

        List<EurderShippingTodayResponse> result = EurderMapper.toListShippingTodayResponse(List.of(eurder), TODAY);

        assertThat(result.get(0).itemGroup())
                .extracting(ItemGroupShippingTodayResponse::itemName)
                .containsExactly("Pen", "Lamp");
    }

    @Test
    void toList_oneResponsePerEurder() {
        User otherUser = TestData.newUser();
        Eurder first = new Eurder(List.of(group("Pen", 2.5, 1, TODAY)), user);
        Eurder second = new Eurder(List.of(group("Book", 15.0, 1, TODAY)), otherUser);

        assertThat(EurderMapper.toListShippingTodayResponse(List.of(first, second), TODAY)).hasSize(2);
    }

    @Test
    void toList_noEurders_returnsEmptyList() {
        assertThat(EurderMapper.toListShippingTodayResponse(List.of(), TODAY)).isEmpty();
    }
}