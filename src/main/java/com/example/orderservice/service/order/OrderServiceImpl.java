package com.example.orderservice.service.order;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.Role;
import com.example.orderservice.domain.Status;
import com.example.orderservice.domain.User;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.UserEntity;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.repository.exception.OrderNotFoundException;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.UserService;
import com.example.orderservice.service.user.OrderMapper;
import com.example.orderservice.service.user.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::fromModel);
    }

    @Override
    public Page<Order> findByUsername(String username, Pageable pageable) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username \"%s\" not found".formatted(username)));

        Page<OrderEntity> orders = orderRepository.findByUserId(userEntity.getId(), pageable);

        return orders.map(orderMapper::fromModel);
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

    @Transactional
    @Override
    public void deleteOrder(UUID id) throws AccessDeniedException {
        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = orderEntity.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to delete this order");
        }

        UserEntity userEntity = orderEntity.getUser();
        User user = userMapper.fromModel(userEntity);

        user.removeOrder(orderEntity.getId());

        UserEntity updatedUserEntity = userMapper.toModel(user);
        userRepository.save(updatedUserEntity);

        log.info("Order {} deleted by user {}", id, currentUser.getUsername());
    }
}
