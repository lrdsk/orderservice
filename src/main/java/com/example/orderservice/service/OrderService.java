package com.example.orderservice.service;

import com.example.orderservice.domain.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Order> findAll();
    Order changeStatus(UUID id, String status);
}
