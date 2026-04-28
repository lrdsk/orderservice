package com.example.orderservice.service;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.RegisterRequestDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User register(RegisterRequestDTO request);

    User findInformationByUsername(String username);

    List<User> findAll();

    void delete(UUID id);

    void addNewOrder(String username, OrderRequestDTO orderRequestDTO);

    User getCurrentUser();
}
