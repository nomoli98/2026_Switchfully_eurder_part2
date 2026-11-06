package com.application.eurder2.data;

import com.application.eurder2.domain.*;
import com.application.eurder2.enums.Role;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

public final class TestData {

    private static int counter = 0;

    private TestData() {
    }

    public static Address newAddress() {
        return new Address("Main Street", "12A", "Brussels", "1000", "Belgium");
    }

    /**
     * Each call gives a unique email and phone number.
     */
    public static User newUser() {
        counter++;
        User user = new User();
        user.setFirstname("Nora");
        user.setLastname("Petry");
        user.setEmail("user" + counter + "@test.com");
        user.setPassword("hashed-password");
        user.setPhoneNumber(String.format("+3247%07d", counter)); // 12 chars
        user.setAddress(newAddress());
        user.setRole(Role.values()[0]); // replace with e.g. Role.CUSTOMER
        return user;
    }

    public static Item newItem(String name, double price, int stock) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(name + " description");
        item.setPrice(price);
        item.setStock(stock);
        return item;
    }

    /**
     * Saves a user and an order with one item group for the given item.
     */
    public static Eurder persistEurder(EntityManager em,
                                       Item item,
                                       int amount,
                                       LocalDate shippingDate) {
        User user = newUser();
        em.persist(user);
        if (item.getId() == null) {
            em.persist(item);
        }
        Eurder eurder = new Eurder();
        eurder.setUser(user);
        ItemGroup group = new ItemGroup(item, amount);
        group.setShippingDate(shippingDate);
        eurder.addItemGroup(group);
        em.persist(eurder);
        return eurder;
    }

    /**
     * Writes pending changes to the DB and empties the cache, so the next read really hits the DB.
     */
    public static void flushAndClear(EntityManager em) {
        em.flush();
        em.clear();
    }

    public static long count(EntityManager em, Class<?> entity) {
        return em.createQuery("select count(e) from " + entity.getSimpleName() + " e", Long.class)
                 .getSingleResult();
    }
}
