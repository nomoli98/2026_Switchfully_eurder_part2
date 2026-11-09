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

import java.util.List;

import static com.application.eurder2.data.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("EurderRepository.getByUser")
class EurderRepositoryGetByUserTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private EurderRepository repository;

    private User customer;
    private User other;
    private Item pen;

    @BeforeEach
    void setUp() {
        customer = newUser();
        other = newUser();
        pen = newItem("Pen", 2.5, 10);
        em.persist(customer);
        em.persist(other);
        em.persist(pen);
    }

    private Eurder saveEurder(User user, int amount) {
        Eurder eurder = new Eurder(List.of(new ItemGroup(pen, amount)), user);
        em.persist(eurder);
        return eurder;
    }

    @Test
    @DisplayName("Given orders of 2 users, then only the orders of the asked user are returned")
    void returnsOnlyOrdersOfUser() {
        Eurder first = saveEurder(customer, 1);
        Eurder second = saveEurder(customer, 2);
        saveEurder(other, 3);
        flushAndClear(em);

        List<Eurder> found = repository.findByUserOrderByOrderDateDesc(customer);

        assertThat(found).extracting(Eurder::getId)
                         .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Given a user without orders, then an empty list is returned")
    void userWithoutOrders_returnsEmptyList() {
        saveEurder(other, 1);
        flushAndClear(em);

        assertThat(repository.findByUserOrderByOrderDateDesc(customer)).isEmpty();
    }

    @Test
    @DisplayName("Given a saved order, then its item groups and price at order are loaded")
    void loadsItemGroupsWithPriceAtOrder() {
        saveEurder(customer, 4);
        flushAndClear(em);

        Eurder found = repository.findByUserOrderByOrderDateDesc(customer).get(0);

        assertThat(found.getItemGroups()).hasSize(1);
        assertThat(found.getItemGroups().get(0).getItemName()).isEqualTo("Pen");
        assertThat(found.getTotalPrice()).isEqualTo(10.0);
    }
}