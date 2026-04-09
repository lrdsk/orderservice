package com.example.orderservice.service;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.RegisterRequestDTO;

public interface UserService {
    User registerUser(RegisterRequestDTO request);
    User getUserInformation(String username);
}
