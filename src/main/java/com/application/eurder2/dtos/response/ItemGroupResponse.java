package com.application.eurder2.dtos.response;

import java.time.LocalDate;
import java.util.UUID;

public record ItemGroupResponse(String itemName,
                                int amount,
                                double groupPrice,
                                LocalDate shippingDate) {

}
