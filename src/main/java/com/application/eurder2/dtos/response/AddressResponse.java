package com.application.eurder2.dtos.response;

public record AddressResponse(String street,
                              String number,
                              String city,
                              String postalCode,
                              String country) {
}
