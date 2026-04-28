package com.example.orderservice.dto;

import java.time.LocalDateTime;

public record OrderResponseDTO(
        String description,
        String status,
        LocalDateTime createdAt) {
}
