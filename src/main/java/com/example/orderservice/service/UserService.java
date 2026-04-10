package com.example.orderservice.service;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.RegisterRequestDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User register(RegisterRequestDTO request);
    User getInformationByUsername(String username);
    List<User> findAll();
    void delete(UUID id);

}
