package com.example.orderservice.service;

import com.example.orderservice.dto.AuthResponseDTO;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {
    AuthResponseDTO auth(@NotBlank String username, @NotBlank String password);
}
