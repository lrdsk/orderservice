package com.example.orderservice.service.order;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.Status;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.exception.OrderNotFoundException;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.user.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    @Override
    public Order changeStatus(UUID id, String status) {
        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id \"%s\" not found".formatted(id)));

        Order order = orderMapper.fromModel(orderEntity);

        order.changeStatus(Status.valueOf(status));

        OrderEntity updatedOrderEntity = orderMapper.toModel(order, orderEntity.getUser());
        orderRepository.save(updatedOrderEntity);

        return order;
    }
}
