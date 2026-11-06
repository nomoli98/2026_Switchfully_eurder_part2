package com.application.eurder2.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ItemGroupTest {

    @Test
    void copiesItemPriceAtOrderTime() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4);

        assertThat(group.getPriceAtOrder()).isEqualTo(2.5);
    }

    @Test
    void priceAtOrder_doesNotChange_whenItemPriceChangesLater() {
        Item pen = new Item("Pen", "Blue pen", 2.5, 10);
        ItemGroup group = new ItemGroup(pen, 4);

        pen.setPrice(99.0);

        assertThat(group.getPriceAtOrder()).isEqualTo(2.5);
        assertThat(group.getGroupPrice()).isEqualTo(10.0);
    }

    @Test
    void groupPrice_isPriceTimesAmount() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4);

        assertThat(group.getGroupPrice()).isEqualTo(10.0);
    }

    @Test
    void enoughStock_shipsTomorrow() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 4);

        assertThat(group.getShippingDate()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    void amountEqualToStock_shipsTomorrow() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 10);

        assertThat(group.getShippingDate()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    void notEnoughStock_shipsInSevenDays() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 11);

        assertThat(group.getShippingDate()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void constructor_doesNotChangeStock() {
        Item pen = new Item("Pen", "Blue pen", 2.5, 10);

        new ItemGroup(pen, 4);

        assertThat(pen.getStock()).isEqualTo(10);
    }

    @Test
    void itemName_comesFromItem() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 1);

        assertThat(group.getItemName()).isEqualTo("Pen");
    }

    @Test
    void newGroup_hasNoEurderYet() {
        ItemGroup group = new ItemGroup(new Item("Pen", "Blue pen", 2.5, 10), 1);

        assertThat(group.getEurder()).isNull();
    }
}