package com.application.eurder2.repository;

import com.application.eurder2.domain.Eurder;
import com.application.eurder2.domain.Item;
import com.application.eurder2.domain.ItemGroup;
import com.application.eurder2.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.application.eurder2.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("EurderRepository.findAllWithItemGroupShippingOn")
class EurderRepositoryShippingOnTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 9);

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private EurderRepository repository;

    private User customer;
    private Item pen;
    private Item book;

    @BeforeEach
    void setUp() {
        customer = newUser();
        pen = newItem("Pen", 2.5, 10);
        book = newItem("Book", 15.0, 10);
        em.persist(customer);
        em.persist(pen);
        em.persist(book);
    }

    private ItemGroup group(Item item, LocalDate shippingDate) {
        ItemGroup group = new ItemGroup(item, 1);
        group.setShippingDate(shippingDate);
        return group;
    }

    private Eurder saveEurder(ItemGroup... groups) {
        Eurder eurder = new Eurder(List.of(groups), customer);
        em.persist(eurder);
        return eurder;
    }

    @Test
    @DisplayName("Given orders on different dates, then only orders with a group on that date are returned")
    void returnsOnlyEurdersWithGroupOnDate() {
        Eurder onDate = saveEurder(group(pen, DATE));
        Eurder dayAfter = saveEurder(group(pen, DATE.plusDays(1)));
        Eurder dayBefore = saveEurder(group(pen, DATE.minusDays(1)));
        flushAndClear(em);

        List<Eurder> found = repository.findAllWithItemGroupShippingOn(DATE);

        assertThat(found).extracting(Eurder::getId)
                         .contains(onDate.getId())
                         .doesNotContain(dayAfter.getId(), dayBefore.getId());
    }

    @Test
    @DisplayName("Given an order with 2 groups on the date, then the order is returned once")
    void eurderWithSeveralGroupsOnDate_returnedOnce() {
        Eurder eurder = saveEurder(group(pen, DATE), group(book, DATE));
        flushAndClear(em);

        List<Eurder> found = repository.findAllWithItemGroupShippingOn(DATE);

        assertThat(found).filteredOn(e -> e.getId().equals(eurder.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Given a mixed order, then it is returned with all its groups, user and items loaded")
    void mixedEurder_loadsGroupsUserAndItems() {
        Eurder mixed = saveEurder(group(pen, DATE), group(book, DATE.plusDays(7)));
        flushAndClear(em);

        Eurder found = repository.findAllWithItemGroupShippingOn(DATE).stream()
                                 .filter(e -> e.getId().equals(mixed.getId()))
                                 .findFirst().orElseThrow();
        em.clear(); // detach: lazy fields must already be loaded by the entity graph

        assertThat(found.getItemGroups()).extracting(ItemGroup::getItemName)
                                         .containsExactlyInAnyOrder("Pen", "Book");
        assertThat(found.getUser().getAddress().city()).isEqualTo("Brussels");
    }

    @Test
    @DisplayName("Given no group on the date, then an empty list is returned")
    void nothingOnDate_returnsEmptyList() {
        saveEurder(group(pen, DATE.plusDays(1)));
        flushAndClear(em);

        assertThat(repository.findAllWithItemGroupShippingOn(DATE)).isEmpty();
    }
}