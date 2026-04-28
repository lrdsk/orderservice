package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderRequestDTO(@NotBlank String description) {
}
