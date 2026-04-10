package com.example.orderservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminOrderResponseDTO(UUID userId,
                                    String description,
                                    String status,
                                    LocalDateTime createdAt) {
}
