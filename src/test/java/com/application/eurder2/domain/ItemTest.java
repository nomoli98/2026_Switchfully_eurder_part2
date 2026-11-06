package com.application.eurder2.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    private final Item pen = new Item("Pen", "Blue pen", 2.5, 10);

    @Test
    void hasSufficientStockFor_lessThanStock_isTrue() {
        assertThat(pen.hasSufficientStockFor(9)).isTrue();
    }

    @Test
    void hasSufficientStockFor_exactStock_isTrue() {
        assertThat(pen.hasSufficientStockFor(10)).isTrue();
    }

    @Test
    void hasSufficientStockFor_moreThanStock_isFalse() {
        assertThat(pen.hasSufficientStockFor(11)).isFalse();
    }

    @Test
    void decreaseStock_subtractsAmount() {
        pen.decreaseStock(3);
        assertThat(pen.getStock()).isEqualTo(7);
    }

    @Test
    void decreaseStock_neverGoesBelowZero() {
        pen.decreaseStock(15);
        assertThat(pen.getStock()).isZero();
    }
}