package com.example.orderservice.domain;

import java.util.UUID;

/**
 * Фабрика для создания доменных объектов {@link Order}.
 * <p>
 * Предоставляет статические методы для создания заказов с автоматической
 * генерацией идентификатора или с указанием конкретного ID.
 * Используется в сервисах для создания новых заказов.
 * </p>
 */
public final class OrderFactory {
    private OrderFactory() {
    }

    public static Order createOrder(String description, UUID userId) {
        return new Order(
                UUID.randomUUID(),
                description,
                Status.CREATED,
                userId);
    }

    public static Order createOrder(UUID id, String description, Status status, UUID userId) {
        return new Order(
                id,
                description,
                status,
                userId);
    }
}
