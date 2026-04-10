package com.example.orderservice.domain;

import java.util.UUID;

public record User(
        UUID id,
        String username,
        String password,
        Role role
) {
}
