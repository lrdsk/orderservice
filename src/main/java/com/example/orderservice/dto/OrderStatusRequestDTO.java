package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusRequestDTO(@NotBlank String status) {
}
