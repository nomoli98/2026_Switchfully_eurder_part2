package com.application.eurder2.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.UUID;

@Entity
@Table(name = "eurders")
public class Eurder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "eurder_id", nullable = false)
    private UUID id;

    @OneToMany(mappedBy = "eurder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemGroup> itemGroups = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_user_id")
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @PrePersist
    void onCreate() {
        this.orderDate = LocalDateTime.now();
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public Eurder() {
    }

    public Eurder(List<ItemGroup> itemGroups, User user) {
        this.user = user;
        itemGroups.forEach(this::addItemGroup);
    }

    public UUID getId() {
        return id;
    }

    public List<ItemGroup> getItemGroups() {
        return Collections.unmodifiableList(itemGroups);
    }

    public void addItemGroup(ItemGroup group) {
        itemGroups.add(group);
        group.setEurder(this);
    }

    public double getTotalPrice() {
        return itemGroups.stream().mapToDouble(ItemGroup::getGroupPrice).sum();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Eurder other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
