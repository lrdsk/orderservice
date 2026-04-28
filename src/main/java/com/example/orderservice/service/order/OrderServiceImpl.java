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
import com.example.orderservice.utils.mapper.OrderMapper;
import com.example.orderservice.utils.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

/**
 * Реализация сервиса для управления заказами.
 * <p>
 * Этот класс предоставляет конкретную реализацию методов {@link OrderService},
 * используя JPA-репозитории и мапперы для преобразования между сущностями и доменными объектами.
 * </p>
 *
 * @see OrderService
 * @see OrderRepository
 * @see OrderMapper
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    /**
     * Возвращает страницу всех заказов с поддержкой пагинации.
     *
     * @param pageable параметры пагинации (номер страницы, размер, сортировка)
     * @return страница доменных объектов {@link Order}
     */
    @Override
    public Page<Order> findAll(Pageable pageable) {
        log.debug("Fetching all orders, pageable: {}", pageable);
        Page<Order> orders = orderRepository.findAll(pageable)
                .map(orderMapper::fromModel);
        log.debug("Found {} orders", orders.getTotalElements());
        return orders;
    }

    /**
     * Возвращает страницу заказов, принадлежащих пользователю с указанным именем.
     *
     * @param username имя пользователя (логин)
     * @param pageable параметры пагинации
     * @return страница заказов пользователя
     * @throws UsernameNotFoundException если пользователь с таким username не найден
     */
    @Override
    public Page<Order> findByUsername(String username, Pageable pageable) {
        log.debug("Fetching orders for username: {}, pageable: {}", username, pageable);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username \"%s\" not found".formatted(username)));

        Page<OrderEntity> orders = orderRepository.findByUserId(userEntity.getId(), pageable);

        log.debug("Found {} orders for user {}", orders.getTotalElements(), username);

        return orders.map(orderMapper::fromModel);
    }

    /**
     * Изменяет статус существующего заказа.
     *
     * @param id     уникальный идентификатор заказа (UUID)
     * @param status новое строковое представление статуса (должно соответствовать {@link Status})
     * @return доменный объект {@link Order} с обновлённым статусом
     * @throws OrderNotFoundException если заказ с указанным id не существует
     * @throws IllegalArgumentException если переданная строка статуса не соответствует ни одному значению {@link Status}
     */
    @Override
    public Order changeStatus(UUID id, String status) {
        log.info("Changing status of order {} to {}", id, status);
        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id \"%s\" not found".formatted(id)));

        Order order = orderMapper.fromModel(orderEntity);

        order.changeStatus(Status.valueOf(status));

        OrderEntity updatedOrderEntity = orderMapper.toModel(order, orderEntity.getUser());
        orderRepository.save(updatedOrderEntity);

        log.info("Order {} status changed to {}", id, status);
        return order;
    }

    /**
     * Удаляет заказ по идентификатору.
     * <p>
     * Удаление разрешено только администратору или владельцу заказа.
     * При удалении заказ также удаляется из списка заказов пользователя.
     * </p>
     *
     * @param id идентификатор заказа (UUID)
     * @throws AccessDeniedException если текущий пользователь не является владельцем заказа и не имеет роли ADMIN
     * @throws OrderNotFoundException если заказ с указанным id не существует
     */
    @Transactional
    @Override
    public void deleteOrder(UUID id) throws AccessDeniedException {
        log.info("Attempting to delete order {}", id);
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

        log.info("Order {} successfully deleted by user {}", id, currentUser.getUsername());
    }
}
