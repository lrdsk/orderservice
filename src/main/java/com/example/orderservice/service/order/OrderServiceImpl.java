package com.example.orderservice.service.order;

import com.example.orderservice.domain.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.user.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::fromModel)
                .toList();
    }
}
