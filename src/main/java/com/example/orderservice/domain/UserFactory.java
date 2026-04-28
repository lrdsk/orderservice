package com.example.orderservice.domain;

import java.util.List;
import java.util.UUID;

/**
 * Фабрика для создания доменных объектов {@link User}.
 * <p>
 * Предоставляет методы для создания новых пользователей (с автоматической
 * генерацией UUID) или восстановления существующих (с указанием ID и роли в виде строки).
 * </p>
 */
public final class UserFactory {
    private UserFactory() {

    }

    public static User createUser(
            String username,
            String password,
            Role role,
            List<Order> orders) {
        return new User(UUID.randomUUID(), username, password, role, orders);
    }

    public static User createUser(
            UUID id,
            String username,
            String password,
            String role,
            List<Order> orders) {
        return new User(id, username, password, Role.valueOf(role), orders);
    }
}
