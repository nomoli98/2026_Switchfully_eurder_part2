package com.application.eurder2.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.application.eurder2.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("ItemGroup persistence")
class ItemGroupPersistenceTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("Given an order with one item group, when saved and reloaded, then amount, price and shipping date are kept")
    void savesItemGroup() {
        Item pen = newItem("Pen", 2.0, 10);
        LocalDate shippingDate = LocalDate.of(2026, 12, 24);
        Eurder eurder = persistEurder(em, pen, 3, shippingDate);
        flushAndClear(em);

        ItemGroup found = em.find(Eurder.class, eurder.getId()).getItemGroups().get(0);

        assertThat(found.getId()).isNotNull();
        assertThat(found.getAmount()).isEqualTo(3);
        assertThat(found.getPriceAtOrder()).isEqualTo(2.0);
        assertThat(found.getShippingDate()).isEqualTo(shippingDate);
    }

    @Test
    @DisplayName("Given a saved item group, when reloaded, then it points to the right item")
    void linksToItem() {
        Item pen = newItem("Pen", 2.0, 10);
        Eurder eurder = persistEurder(em, pen, 1, LocalDate.now());
        flushAndClear(em);

        ItemGroup found = em.find(Eurder.class, eurder.getId()).getItemGroups().get(0);

        assertThat(found.getItem().getId()).isEqualTo(pen.getId());
        assertThat(found.getItemName()).isEqualTo("Pen");
    }

    @Test
    @DisplayName("Given a saved item group, when reloaded, then it points to its order")
    void linksToEurder() {
        Eurder eurder = persistEurder(em, newItem("Pen", 2.0, 10), 1, LocalDate.now());
        flushAndClear(em);

        ItemGroup found = em.find(Eurder.class, eurder.getId()).getItemGroups().get(0);

        assertThat(found.getEurder().getId()).isEqualTo(eurder.getId());
    }

    @Test
    @DisplayName("Given 4 items at 2.5, when the group price is calculated, then it is 10")
    void calculatesGroupPrice() {
        ItemGroup group = new ItemGroup(newItem("Pen", 2.5, 10), 4);

        assertThat(group.getGroupPrice()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Given a saved order, when the item price changes, then the group keeps the old price")
    void priceStaysFrozenWhenItemPriceChanges() {
        Item pen = newItem("Pen", 2.0, 10);
        Eurder eurder = persistEurder(em, pen, 1, LocalDate.now());
        flushAndClear(em);

        em.find(Item.class, pen.getId()).setPrice(99.0);
        flushAndClear(em);

        ItemGroup found = em.find(Eurder.class, eurder.getId()).getItemGroups().get(0);
        assertThat(found.getPriceAtOrder()).isEqualTo(2.0);
        assertThat(found.getGroupPrice()).isEqualTo(2.0);
        assertThat(found.getItem().getPrice()).isEqualTo(99.0);
    }

    @Test
    @DisplayName("Given one item, when it is ordered twice, then there are 2 groups and 1 item")
    void sameItemCanBeInSeveralGroups() {
        Item pen = newItem("Pen", 2.0, 10);
        persistEurder(em, pen, 1, LocalDate.now());
        persistEurder(em, pen, 2, LocalDate.now());
        flushAndClear(em);

        assertThat(count(em, ItemGroup.class)).isEqualTo(2);
        assertThat(count(em, Item.class)).isEqualTo(1);
    }

    @Test
    @DisplayName("Given an item group with amount 0, when saved, then an error is thrown")
    void rejectsZeroAmount() {
        assertThatThrownBy(() -> {
            persistEurder(em, newItem("Pen", 2.0, 10), 0, LocalDate.now());
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given an item group without an order, when saved, then an error is thrown")
    void rejectsGroupWithoutEurder() {
        Item pen = newItem("Pen", 2.0, 10);
        em.persist(pen);
        ItemGroup group = new ItemGroup(pen, 1);

        assertThatThrownBy(() -> {
            em.persist(group);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given an item group without a shipping date, when saved, then an error is thrown")
    void rejectsMissingShippingDate() {
        assertThatThrownBy(() -> {
            persistEurder(em, newItem("Pen", 2.0, 10), 1, null);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a saved item group, when reloaded, then it equals the original")
    void sameRowIsEqualAfterReload() {
        Eurder eurder = persistEurder(em, newItem("Pen", 2.0, 10), 1, LocalDate.now());
        ItemGroup original = eurder.getItemGroups().get(0);
        flushAndClear(em);

        ItemGroup reloaded = em.find(ItemGroup.class, original.getId());

        assertThat(reloaded).isEqualTo(original);
        assertThat(reloaded.hashCode()).isEqualTo(original.hashCode());
    }
}