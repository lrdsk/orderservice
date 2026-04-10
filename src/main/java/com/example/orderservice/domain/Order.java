package com.example.orderservice.domain;

import java.util.UUID;

public record Order(
        UUID id,
        String description,
        Status status,
        UUID userId) {
}
