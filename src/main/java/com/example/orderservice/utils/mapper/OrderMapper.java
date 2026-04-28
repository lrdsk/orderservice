package com.example.orderservice.utils.mapper;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderFactory;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.Status;
import com.example.orderservice.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования между {@link OrderEntity}, {@link Order} .
 * Используется в сервисах для изоляции JPA-сущностей от доменной логики.
 */
@Component
public class OrderMapper {
    /**
     * Преобразует доменный объект в JPA-сущность.
     * @param order домен
     * @param userEntity связь с пользователем (владельцем заказа)
     * @return сущность для сохранения в БД
     */
    public OrderEntity toModel(Order order, UserEntity userEntity) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(order.getId());
        orderEntity.setDescription(order.getDescription());
        orderEntity.setStatus(Status.valueOf(order.getStatus().toString()));
        orderEntity.setUser(userEntity);

        return orderEntity;
    }

    /**
     * Преобразует JPA-сущность в доменный объект.
     * @param orderEntity сущность из БД
     * @return домен Order (с полным состоянием)
     */
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
