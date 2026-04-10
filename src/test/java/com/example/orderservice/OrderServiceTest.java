package com.example.orderservice;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.Role;
import com.example.orderservice.domain.Status;
import com.example.orderservice.domain.User;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.UserEntity;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.repository.exception.OrderNotFoundException;
import com.example.orderservice.service.UserService;
import com.example.orderservice.service.order.OrderServiceImpl;
import com.example.orderservice.service.user.OrderMapper;
import com.example.orderservice.service.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private OrderMapper orderMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID orderId;
    private UUID userId;
    private OrderEntity orderEntity;
    private UserEntity userEntity;
    private Order order;
    private User user;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername("john");
        userEntity.setRole(com.example.orderservice.entity.Role.USER);
        orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setDescription("Test order");
        orderEntity.setStatus(com.example.orderservice.entity.Status.CREATED);
        orderEntity.setUser(userEntity);
        userEntity.setOrders(List.of(orderEntity));

        user = new User(userId, "john", "encoded", Role.USER, List.of());
        order = new Order(orderId, "Test order", com.example.orderservice.domain.Status.CREATED, userId);
    }

    @Test
    void findAll_ShouldReturnPageOfOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> entityPage = new PageImpl<>(List.of(orderEntity), pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(entityPage);
        when(orderMapper.fromModel(orderEntity)).thenReturn(order);

        Page<Order> result = orderService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().size() == 1);
        assertThat(result.getContent().getFirst()).isEqualTo(order);
        verify(orderRepository).findAll(pageable);
        verify(orderMapper).fromModel(orderEntity);
    }

    @Test
    void findByUsername_ShouldReturnOrdersForUser() {
        String username = "john";
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        Page<OrderEntity> entityPage = new PageImpl<>(List.of(orderEntity), pageable, 1);
        when(orderRepository.findByUserId(userId, pageable)).thenReturn(entityPage);
        when(orderMapper.fromModel(orderEntity)).thenReturn(order);

        Page<Order> result = orderService.findByUsername(username, pageable);

        assertThat(result.getContent()).containsExactly(order);
        verify(userRepository).findByUsername(username);
        verify(orderRepository).findByUserId(userId, pageable);
    }

    @Test
    void findByUsername_WhenUserNotFound_ShouldThrowException() {
        String username = "unknown";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findByUsername(username, Pageable.unpaged()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with username \"unknown\" not found");
    }

    @Test
    void changeStatus_ShouldUpdateOrderStatusAndSave() {
        String newStatus = "IN_PROGRESS";
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(orderMapper.fromModel(orderEntity)).thenReturn(order);
        when(orderMapper.toModel(any(Order.class), any(UserEntity.class))).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);

        Order result = orderService.changeStatus(orderId, newStatus);

        assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
        verify(orderRepository).findById(orderId);
        verify(orderMapper).fromModel(orderEntity);
        verify(orderMapper).toModel(order, userEntity);
        verify(orderRepository).save(orderEntity);
    }

    @Test
    void changeStatus_WhenOrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.changeStatus(orderId, "IN_PROGRESS"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with id");
    }

    @Test
    void deleteOrder_AsAdmin_ShouldDeleteSuccessfully() throws AccessDeniedException {
        User adminUser = new User(userId, "admin", "encoded", Role.ADMIN, List.of());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(userMapper.fromModel(userEntity)).thenReturn(user);
        when(userMapper.toModel(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        orderService.deleteOrder(orderId);

        verify(orderRepository).findById(orderId);
        verify(userService).getCurrentUser();
        verify(userMapper).fromModel(userEntity);
        verify(userMapper).toModel(any(User.class));
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void deleteOrder_AsOwner_ShouldDeleteSuccessfully() throws AccessDeniedException {
        User ownerUser = new User(userId, "john", "encoded", Role.USER, List.of());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(userService.getCurrentUser()).thenReturn(ownerUser);
        when(userMapper.fromModel(userEntity)).thenReturn(user);
        when(userMapper.toModel(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        orderService.deleteOrder(orderId);

        verify(userService).getCurrentUser();
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void deleteOrder_AsNonOwnerAndNotAdmin_ShouldThrowAccessDenied() {
        User otherUser = new User(UUID.randomUUID(), "other", "encoded", Role.USER, List.of());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not allowed to delete this order");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrder_WhenOrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
