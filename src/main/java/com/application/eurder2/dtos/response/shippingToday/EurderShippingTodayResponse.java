package com.application.eurder2.dtos.response.shippingToday;


import com.application.eurder2.dtos.response.AddressResponse;

import java.util.List;

public record EurderShippingTodayResponse(List<ItemGroupShippingTodayResponse> itemGroup,
                                          AddressResponse shippingAddress) {}
