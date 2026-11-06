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
@DisplayName("User persistence")
class UserPersistenceTest {

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("Given a valid user, when saved and reloaded, then all fields are kept")
    void savesUser() {
        User user = newUser();
        em.persist(user);
        flushAndClear(em);

        User found = em.find(User.class, user.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getFirstname()).isEqualTo("Nora");
        assertThat(found.getLastname()).isEqualTo("Petry");
        assertThat(found.getEmail()).isEqualTo(user.getEmail());
        assertThat(found.getPassword()).isEqualTo("hashed-password");
        assertThat(found.getPhoneNumber()).isEqualTo(user.getPhoneNumber());
        assertThat(found.getRole()).isEqualTo(user.getRole());
    }

    @Test
    @DisplayName("Given a saved user, when the last name changes, then the change is saved")
    void updatesUser() {
        User user = newUser();
        em.persist(user);
        flushAndClear(em);

        em.find(User.class, user.getId()).setLastname("Changed");
        flushAndClear(em);

        assertThat(em.find(User.class, user.getId()).getLastname()).isEqualTo("Changed");
    }

    @Test
    @DisplayName("Given a saved user, when another user with the same email is saved, then an error is thrown")
    void rejectsDuplicateEmail() {
        User first = newUser();
        User second = newUser();
        second.setEmail(first.getEmail());
        em.persist(first);

        assertThatThrownBy(() -> {
            em.persist(second);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a saved user, when another user with the same phone number is saved, then an error is thrown")
    void rejectsDuplicatePhoneNumber() {
        User first = newUser();
        User second = newUser();
        second.setPhoneNumber(first.getPhoneNumber());
        em.persist(first);

        assertThatThrownBy(() -> {
            em.persist(second);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a phone number longer than 12 characters, when saved, then an error is thrown")
    void rejectsPhoneNumberLongerThan12() {
        User user = newUser();
        user.setPhoneNumber("+3247000000000"); // 14 chars

        assertThatThrownBy(() -> {
            em.persist(user);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a user without a first name, when saved, then an error is thrown")
    void rejectsMissingFirstname() {
        User user = newUser();
        user.setFirstname(null);

        assertThatThrownBy(() -> {
            em.persist(user);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a user without a role, when saved, then an error is thrown")
    void rejectsMissingRole() {
        User user = newUser();
        user.setRole(null);

        assertThatThrownBy(() -> {
            em.persist(user);
            em.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("Given a saved user, when reloaded, then it equals the original")
    void sameRowIsEqualAfterReload() {
        User user = newUser();
        em.persist(user);
        flushAndClear(em);

        User reloaded = em.find(User.class, user.getId());

        assertThat(reloaded).isEqualTo(user);
        assertThat(reloaded.hashCode()).isEqualTo(user.hashCode());
    }

    @Test
    @DisplayName("Given two saved users, when compared, then they are not equal")
    void differentUsersAreNotEqual() {
        User a = newUser();
        User b = newUser();
        em.persist(a);
        em.persist(b);
        em.flush();

        assertThat(a).isNotEqualTo(b);
    }
}