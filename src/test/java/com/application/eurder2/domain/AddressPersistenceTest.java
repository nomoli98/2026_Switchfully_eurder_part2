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

/** Address is embedded in User, so it is tested through a User. */
@SpringBootTest
@Transactional
@DisplayName("Address persistence")
class AddressPersistenceTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("Given a user with an address, when saved and reloaded, then all address fields are kept")
    void savesAllAddressFields() {
        User user = newUser();
        em.persist(user);
        flushAndClear(em);

        Address found = em.find(User.class, user.getId()).getAddress();

        assertThat(found.street()).isEqualTo("Main Street");
        assertThat(found.number()).isEqualTo("12A");
        assertThat(found.city()).isEqualTo("Brussels");
        assertThat(found.postalCode()).isEqualTo("1000");
        assertThat(found.country()).isEqualTo("Belgium");
    }

    @Test
    @DisplayName("Given a house number with letters, when saved and reloaded, then it is kept as text")
    void houseNumberWithLettersIsKept() {
        User user = newUser();
        user.setAddress(new Address("Main Street", "12B bus 3", "Brussels", "1000", "Belgium"));
        em.persist(user);
        flushAndClear(em);

        assertThat(em.find(User.class, user.getId()).getAddress().number()).isEqualTo("12B bus 3");
    }

    @Test
    @DisplayName("Given two addresses with the same values, when compared, then they are equal")
    void equalAddressesAreEqual() {
        assertThat(newAddress()).isEqualTo(newAddress());
    }

    @Test
    @DisplayName("Given an address without a street, when saved, then an error is thrown")
    void rejectsMissingStreet() {
        User user = newUser();
        user.setAddress(new Address(null, "12A", "Brussels", "1000", "Belgium"));

        assertThatThrownBy(() -> {
            em.persist(user);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a user without an address, when saved, then an error is thrown")
    void rejectsMissingAddress() {
        User user = newUser();
        user.setAddress(null);

        assertThatThrownBy(() -> {
            em.persist(user);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }
}