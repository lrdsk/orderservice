package com.example.orderservice.domain;

import lombok.Getter;

import java.util.*;

/**
 * Доменный объект «Пользователь».
 * <p>
 * Содержит данные пользователя (id, имя, пароль, роль) и список его заказов.
 * Обеспечивает неизменяемость списка заказов при доступе через геттер.
 * Предоставляет методы для добавления и удаления заказов с проверками.
 * </p>
 */
public class User {
    @Getter
    private final UUID id;
    @Getter
    private final String username;
    @Getter
    private final String password;
    @Getter
    private final Role role;
    private List<Order> orders;

    User(UUID id, String username, String password, Role role, List<Order> orders) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
    }

    public void addOrder(Order order) {
        if (orders.contains(order)) {
            throw new IllegalStateException("Current user with id \"%s\" already has this order with id \"%s\"".formatted(id, order.getId()));
        }
        orders.add(order);
    }

    public void removeOrder(UUID orderId) {
        orders.stream()
                .filter(order -> order.getId().equals(orderId))
                .findFirst()
                .ifPresent(foundOrder -> orders.remove(foundOrder));
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }
}
