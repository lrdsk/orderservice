package com.example.orderservice.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Order {
    private final UUID id;
    private String description;
    private Status status;
    private final UUID userId;
    private LocalDateTime createdAt;

    public Order(UUID id, String description, Status status, UUID userId) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.userId = userId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
