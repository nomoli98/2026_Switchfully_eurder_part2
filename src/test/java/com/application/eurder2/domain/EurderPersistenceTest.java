package com.application.eurder2.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static com.application.eurder2.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Eurder persistence")
class EurderPersistenceTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("Given an order with a user, when saved and reloaded, then it is linked to that user")
    void savesEurderWithUser() {
        User user = newUser();
        em.persist(user);
        Eurder eurder = new Eurder();
        eurder.setUser(user);
        em.persist(eurder);
        flushAndClear(em);

        Eurder found = em.find(Eurder.class, eurder.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getItemGroups()).isEmpty();
    }

    @Test
    @DisplayName("Given an order with 2 item groups, when only the order is saved, then both groups are saved")
    void savingEurderAlsoSavesItemGroups() {
        User user = newUser();
        Item pen = newItem("Pen", 2.0, 10);
        Item book = newItem("Book", 10.0, 5);
        em.persist(user);
        em.persist(pen);
        em.persist(book);

        Eurder eurder = new Eurder();
        eurder.setUser(user);
        eurder.addItemGroup(new ItemGroup(pen, 3, LocalDate.now()));
        eurder.addItemGroup(new ItemGroup(book, 1, LocalDate.now()));
        em.persist(eurder); // only the eurder: cascade must save the groups
        flushAndClear(em);

        assertThat(count(em, ItemGroup.class)).isEqualTo(2);
        assertThat(em.find(Eurder.class, eurder.getId()).getItemGroups()).hasSize(2);
    }

    @Test
    @DisplayName("Given an order, when an item group is added, then both sides point to each other")
    void addItemGroupSetsBothSides() {
        Eurder eurder = new Eurder();
        ItemGroup group = new ItemGroup(newItem("Pen", 2.0, 10), 1, LocalDate.now());

        eurder.addItemGroup(group);

        assertThat(eurder.getItemGroups()).containsExactly(group);
        assertThat(group.getEurder()).isSameAs(eurder);
    }

    @Test
    @DisplayName("Given an order, when adding to getItemGroups() directly, then an error is thrown")
    void itemGroupsListCannotBeChangedDirectly() {
        Eurder eurder = new Eurder();
        ItemGroup group = new ItemGroup(newItem("Pen", 2.0, 10), 1, LocalDate.now());

        assertThatThrownBy(() -> eurder.getItemGroups().add(group))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Given groups costing 6 and 20, when the total is calculated, then it is 26")
    void calculatesTotalPrice() {
        User user = newUser();
        Item pen = newItem("Pen", 2.0, 10);
        Item book = newItem("Book", 10.0, 5);
        em.persist(user);
        em.persist(pen);
        em.persist(book);

        Eurder eurder = new Eurder();
        eurder.setUser(user);
        eurder.addItemGroup(new ItemGroup(pen, 3, LocalDate.now()));  // 6.0
        eurder.addItemGroup(new ItemGroup(book, 2, LocalDate.now())); // 20.0
        em.persist(eurder);
        flushAndClear(em);

        assertThat(em.find(Eurder.class, eurder.getId()).getTotalPrice()).isEqualTo(26.0);
    }

    @Test
    @DisplayName("Given an order without item groups, when the total is calculated, then it is 0")
    void emptyEurderCostsNothing() {
        assertThat(new Eurder().getTotalPrice()).isZero();
    }

    @Test
    @DisplayName("Given one user, when 2 orders are saved, then there are 2 orders and 1 user")
    void oneUserCanHaveSeveralEurders() {
        User user = newUser();
        em.persist(user);
        for (int i = 0; i < 2; i++) {
            Eurder eurder = new Eurder();
            eurder.setUser(user);
            em.persist(eurder);
        }
        flushAndClear(em);

        assertThat(count(em, Eurder.class)).isEqualTo(2);
        assertThat(count(em, User.class)).isEqualTo(1);
    }

    @Test
    @DisplayName("Given a saved order, when it is deleted, then its groups are deleted but the item and user stay")
    void deletingEurderDeletesItemGroupsButNotItemOrUser() {
        Item pen = newItem("Pen", 2.0, 10);
        Eurder eurder = persistEurder(em, pen, 1, LocalDate.now());
        UUID userId = eurder.getUser().getId();
        flushAndClear(em);

        em.remove(em.find(Eurder.class, eurder.getId()));
        flushAndClear(em);

        assertThat(count(em, ItemGroup.class)).isZero();
        assertThat(em.find(Item.class, pen.getId())).isNotNull();
        assertThat(em.find(User.class, userId)).isNotNull();
    }

    @Test
    @DisplayName("Given a user with an order, when the user is deleted, then an error is thrown")
    void cannotDeleteUserWhoHasEurders() {
        Eurder eurder = persistEurder(em, newItem("Pen", 2.0, 10), 1, LocalDate.now());
        UUID userId = eurder.getUser().getId();
        flushAndClear(em);

        assertThatThrownBy(() -> {
            em.remove(em.find(User.class, userId));
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given an order without a user, when saved, then an error is thrown")
    void rejectsEurderWithoutUser() {
        Eurder eurder = new Eurder();

        assertThatThrownBy(() -> {
            em.persist(eurder);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given an unknown id, when searching for an order, then nothing is found")
    void unknownIdReturnsNull() {
        assertThat(em.find(Eurder.class, UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("Given a saved order, when reloaded, then it equals the original")
    void sameRowIsEqualAfterReload() {
        Eurder eurder = persistEurder(em, newItem("Pen", 2.0, 10), 1, LocalDate.now());
        flushAndClear(em);

        Eurder reloaded = em.find(Eurder.class, eurder.getId());

        assertThat(reloaded).isEqualTo(eurder);
        assertThat(reloaded.hashCode()).isEqualTo(eurder.hashCode());
    }
}