package com.example.orderservice.domain;

import java.util.UUID;

public final class UserFactory {
    private UserFactory() {

    }

    public static User createUser(
            String username,
            String password,
            String role) {
        return new User(UUID.randomUUID(), username, password, Role.valueOf(role));
    }
    public static User createUser(
            UUID id,
            String username,
            String password,
            String role) {
        return new User(id, username, password, Role.valueOf(role));
    }
}
