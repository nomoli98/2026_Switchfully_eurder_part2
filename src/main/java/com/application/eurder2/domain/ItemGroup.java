package com.application.eurder2.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "item_groups")
public class ItemGroup {

    private static final int DAYS_TO_SHIP_WHEN_IN_STOCK = 1;
    private static final int DAYS_TO_SHIP_WHEN_NOT_IN_STOCK = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_group_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_item_id")
    private Item item;

    @Column(name = "item_amount", nullable = false)
    private int amount;

    @Column(name = "price_at_order", nullable = false)
    private double priceAtOrder;

    @Column(name = "shipping_date", nullable = false)
    private LocalDate shippingDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_eurder_id")
    private Eurder eurder;

    public ItemGroup() {
    }

    public ItemGroup(Item item, int amount) {
        this.item = item;
        this.amount = amount;
        this.priceAtOrder = item.getPrice();
        int daysToShip = item.hasSufficientStockFor(amount) ?
                         DAYS_TO_SHIP_WHEN_IN_STOCK :
                         DAYS_TO_SHIP_WHEN_NOT_IN_STOCK;
        this.shippingDate = LocalDate.now().plusDays(daysToShip);
        item.decreaseStock(amount);
    }

    public UUID getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public String getItemName() {
        return item.getName();
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public double getGroupPrice() {
        return priceAtOrder * amount;
    }

    public LocalDate getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(LocalDate shippingDate) {
        this.shippingDate = shippingDate;
    }

    public Eurder getEurder() {
        return eurder;
    }

    public void setEurder(Eurder eurder) {
        this.eurder = eurder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemGroup other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
