package com.example.orderservice.domain;

import java.util.UUID;

public final class OrderFactory {
    private OrderFactory() {}

    public static Order createOrder(String description, UUID userId) {
        return new Order(
                UUID.randomUUID(),
                description,
                Status.CREATED,
                userId);
    }
}
