package com.application.eurder2.mapper;

import com.application.eurder2.domain.Address;
import com.application.eurder2.dtos.request.CreateAddressRequest;
import com.application.eurder2.dtos.response.AddressResponse;

public class AddressMapper {
    public static AddressResponse toResponse(Address address) {
        return new AddressResponse(address.street(),
                                   address.number(),
                                   address.city(),
                                   address.postalCode(),
                                   address.country());
    }

    public static Address toEntity(CreateAddressRequest address) {
        return new Address(address.street(),
                           address.number(),
                           address.city(),
                           address.postalCode(),
                           address.country());
    }


}
