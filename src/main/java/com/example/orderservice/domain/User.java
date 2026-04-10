package com.example.orderservice.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private final String username;
    private final String password;
    private final Role role;
    private List<Order> orders;

    public User(UUID id, String username, String password, Role role, List<Order> orders) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
    }

    public void addOrder(Order order) {
        if(orders.contains(order)) {
            throw new IllegalStateException("Current user with id \"%s\" already has this order with id \"%s\"".formatted(id, order.getId()));
        }
        orders.add(order);
    }
}
