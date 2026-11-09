package com.application.eurder2.dtos.response.stockOverview;

import com.application.eurder2.enums.StockIndicator;

import java.util.UUID;

public record ItemStockOverviewResponse(UUID itemId,
                                        String itemName,
                                        int itemStock,
                                        StockIndicator stockIndicator) {
}
