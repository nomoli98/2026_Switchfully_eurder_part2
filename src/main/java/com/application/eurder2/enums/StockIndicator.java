package com.application.eurder2.enums;

public enum StockIndicator {
    STOCK_LOW (5),
    STOCK_MEDIUM (10),
    STOCK_HIGH (Integer.MAX_VALUE);

    private final int upperBound;

    StockIndicator(int upperBound) {
        this.upperBound = upperBound;
    }

    public static StockIndicator from(int itemStock) {
        for (StockIndicator s : values()) {
            if (itemStock < s.upperBound) {
                return s;
            }
        }
        return STOCK_HIGH;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public int getLowerBound() {
        return ordinal() == 0 ? 0 : values()[ordinal() - 1].upperBound;
    }

}
