package com.application.eurder2.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.application.eurder2.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Item persistence")
class ItemPersistenceTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("Given a valid item, when saved and reloaded, then all fields are kept")
    void savesItem() {
        Item item = newItem("Pen", 1.50, 10);
        em.persist(item);
        flushAndClear(em);

        Item found = em.find(Item.class, item.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Pen");
        assertThat(found.getDescription()).isEqualTo("Pen description");
        assertThat(found.getPrice()).isEqualTo(1.50);
        assertThat(found.getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("Given a saved item with stock 10, when stock is reduced by 3, then 7 is saved")
    void reducedStockIsSaved() {
        Item item = newItem("Pen", 1.50, 10);
        em.persist(item);
        item.reduceStock(3);
        flushAndClear(em);

        assertThat(em.find(Item.class, item.getId()).getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("Given an item with stock 5, when checking 5 and 6, then only 5 is sufficient")
    void hasSufficientStock() {
        Item item = newItem("Pen", 1.50, 5);

        assertThat(item.hasSufficientStockFor(5)).isTrue();
        assertThat(item.hasSufficientStockFor(6)).isFalse();
    }

    @Test
    @DisplayName("Given an item with price 0, when saved, then it is accepted")
    void zeroPriceIsAllowed() {
        Item item = newItem("Free sample", 0, 10);
        em.persist(item);
        flushAndClear(em);

        assertThat(em.find(Item.class, item.getId()).getPrice()).isZero();
    }

    @Test
    @DisplayName("Given an item with a negative price, when saved, then an error is thrown")
    void rejectsNegativePrice() {
        Item item = newItem("Pen", -1, 10);

        assertThatThrownBy(() -> {
            em.persist(item);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given an item without a name, when saved, then an error is thrown")
    void rejectsMissingName() {
        Item item = newItem("Pen", 1.0, 10);
        item.setName(null);

        assertThatThrownBy(() -> {
            em.persist(item);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a saved item, when reloaded, then it equals the original")
    void sameRowIsEqualAfterReload() {
        Item item = newItem("Pen", 1.0, 1);
        em.persist(item);
        flushAndClear(em);

        Item reloaded = em.find(Item.class, item.getId());

        assertThat(reloaded).isEqualTo(item);
        assertThat(reloaded.hashCode()).isEqualTo(item.hashCode());
    }

    @Test
    @DisplayName("Given two unsaved items with the same values, when compared, then they are not equal")
    void unsavedItemsAreNotEqual() {
        assertThat(newItem("Pen", 1.0, 1)).isNotEqualTo(newItem("Pen", 1.0, 1));
    }

    @Test
    @DisplayName("Given two saved items, when compared, then they are not equal")
    void differentItemsAreNotEqual() {
        Item a = newItem("Pen", 1.0, 1);
        Item b = newItem("Pen", 1.0, 1);
        em.persist(a);
        em.persist(b);
        em.flush();

        assertThat(a).isNotEqualTo(b);
    }
}