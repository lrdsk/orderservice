package com.example.orderservice.service;

import com.example.orderservice.domain.Order;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Order> findAll();

    Order changeStatus(UUID id, String status);

    void deleteOrder(UUID id) throws AccessDeniedException;
}
