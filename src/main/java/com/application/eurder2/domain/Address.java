package com.application.eurder2.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        @Column(name = "street", nullable = false) String street,
        @Column(name = "house_number", nullable = false) String number,
        @Column(name = "city", nullable = false) String city,
        @Column(name = "postal_code", nullable = false) String postalCode,
        @Column(name = "country", nullable = false) String country) {
}