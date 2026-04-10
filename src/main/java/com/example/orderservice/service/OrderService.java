package com.example.orderservice.service;

import com.example.orderservice.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

public interface OrderService {

    Page<Order> findAll(Pageable pageable);

    Page<Order> findByUsername(String username, Pageable pageable);

    Order changeStatus(UUID id, String status);

    void deleteOrder(UUID id) throws AccessDeniedException;
}
