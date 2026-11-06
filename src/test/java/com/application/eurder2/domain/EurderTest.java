package com.application.eurder2.domain;

import com.application.eurder2.data.TestData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EurderTest {

    private final User user = TestData.newUser();

    private ItemGroup group(String name, double price, int amount) {
        return new ItemGroup(new Item(name, name + " description", price, 100), amount);
    }

    @Test
    void constructor_linksEveryGroupToTheEurder() {
        ItemGroup pen = group("Pen", 2.5, 4);
        ItemGroup book = group("Book", 15.0, 1);

        Eurder eurder = new Eurder(List.of(pen, book), user);

        assertThat(pen.getEurder()).isSameAs(eurder);
        assertThat(book.getEurder()).isSameAs(eurder);
    }

    @Test
    void constructor_keepsGroupsInOrder() {
        ItemGroup pen = group("Pen", 2.5, 4);
        ItemGroup book = group("Book", 15.0, 1);

        Eurder eurder = new Eurder(List.of(pen, book), user);

        assertThat(eurder.getItemGroups()).containsExactly(pen, book);
    }

    @Test
    void constructor_setsUser() {
        Eurder eurder = new Eurder(List.of(group("Pen", 2.5, 1)), user);

        assertThat(eurder.getUser()).isSameAs(user);
    }

    @Test
    void constructor_acceptsImmutableList_andStillAllowsAdding() {
        Eurder eurder = new Eurder(List.of(group("Pen", 2.5, 1)), user);

        eurder.addItemGroup(group("Book", 15.0, 1));

        assertThat(eurder.getItemGroups()).hasSize(2);
    }

    @Test
    void totalPrice_isSumOfGroupPrices() {
        Eurder eurder = new Eurder(List.of(group("Pen", 2.5, 4), group("Book", 15.0, 2)), user);

        assertThat(eurder.getTotalPrice()).isEqualTo(40.0);
    }

    @Test
    void totalPrice_ofEmptyEurder_isZero() {
        assertThat(new Eurder(List.of(), user).getTotalPrice()).isZero();
    }

    @Test
    void addItemGroup_linksGroupToEurder() {
        Eurder eurder = new Eurder();
        ItemGroup pen = group("Pen", 2.5, 1);

        eurder.addItemGroup(pen);

        assertThat(pen.getEurder()).isSameAs(eurder);
        assertThat(eurder.getItemGroups()).containsExactly(pen);
    }

    @Test
    void getItemGroups_cannotBeModifiedFromOutside() {
        Eurder eurder = new Eurder(List.of(group("Pen", 2.5, 1)), user);

        assertThatThrownBy(() -> eurder.getItemGroups().add(group("Book", 15.0, 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}