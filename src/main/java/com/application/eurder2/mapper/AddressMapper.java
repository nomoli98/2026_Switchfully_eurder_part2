package com.application.eurder2.mapper;

import com.application.eurder2.domain.Address;
import com.application.eurder2.dtos.request.AddressRequest;
import com.application.eurder2.dtos.response.AddressResponse;

public class AddressMapper {
    public static AddressResponse toResponse(Address address) {
        return new AddressResponse(address.street(),
                                   address.number(),
                                   address.city(),
                                   address.postalCode(),
                                   address.country());
    }

    public static Address toEntity(AddressRequest address) {
        return new Address(address.street(),
                           address.number(),
                           address.city(),
                           address.postalCode(),
                           address.country());
    }


}
