package com.example.orderservice.service.user;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderFactory;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.Status;
import com.example.orderservice.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderEntity toModel(Order order, UserEntity userEntity) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(order.getId());
        orderEntity.setDescription(order.getDescription());
        orderEntity.setStatus(Status.valueOf(order.getStatus().toString()));
        orderEntity.setUser(userEntity);

        return orderEntity;
    }

    public Order fromModel(OrderEntity orderEntity) {
        Order order = OrderFactory.createOrder(
                orderEntity.getId(),
                orderEntity.getDescription(),
                com.example.orderservice.domain.Status.valueOf(orderEntity.getStatus().toString()),
                orderEntity.getUser().getId()
        );
        order.setCreatedAt(orderEntity.getCreatedAt());

        return order;
    }
}
