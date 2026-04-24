package com.example.orderservice.service;

import com.example.orderservice.domain.*;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.UserEntity;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.repository.exception.OrderNotFoundException;
import com.example.orderservice.service.order.OrderServiceImpl;
import com.example.orderservice.service.user.OrderMapper;
import com.example.orderservice.service.user.UserMapper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID expectedOrderId;
    private UUID expectedUserId;
    private OrderEntity expectedOrderEntity;
    private UserEntity expectedUserEntity;
    private Order expectedOrder;
    private User expectedUser;

    @BeforeEach
    void setUp() {
        this.expectedUserEntity = getExpectedUserEntity();
        this.expectedOrderEntity = getExpectedOrderEntity(expectedUserEntity);
        this.expectedUserId = expectedUserEntity.getId();
        this.expectedOrderId = expectedOrderEntity.getId();
        setOrdersForExpectedUserEntity(expectedUserEntity, List.of(expectedOrderEntity));

        expectedUser = getExpectedUser(expectedUserId, expectedUserEntity.getUsername());
        expectedOrder = getExpectedOrder(expectedOrderId, expectedUserId);
    }

    private @NonNull Order getExpectedOrder(UUID id, UUID userId) {
        return new Order(id, "Test order", Status.CREATED, userId);
    }

    private @NonNull User getExpectedUser(UUID id, String username) {
        return UserFactory.createUser(id, username, "encoded", "USER", List.of());
    }

    private @NonNull OrderEntity getExpectedOrderEntity(UserEntity userEntity) {
        expectedOrderEntity = new OrderEntity();
        expectedOrderEntity.setId(UUID.fromString("2377466b-4ced-4181-9b8c-2fa647b296c3"));
        expectedOrderEntity.setDescription("Test order");
        expectedOrderEntity.setStatus(com.example.orderservice.entity.Status.CREATED);
        expectedOrderEntity.setUser(userEntity);
        return expectedOrderEntity;
    }

    private @NonNull UserEntity getExpectedUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.fromString("ddc5aed9-7c41-49fe-a36e-514e5faa1017"));
        userEntity.setUsername("Test name");
        userEntity.setRole(com.example.orderservice.entity.Role.USER);

        return userEntity;
    }

    private void setOrdersForExpectedUserEntity(UserEntity userEntity, List<OrderEntity> orderEntities) {
        userEntity.setOrders(orderEntities);
    }

    @Test
    @DisplayName("Должен вернуть список всех заказов")
    void findAll_ShouldReturnPageOfOrders() {
        //given
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> entityPage = new PageImpl<>(List.of(expectedOrderEntity), pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(entityPage);
        when(orderMapper.fromModel(expectedOrderEntity)).thenReturn(expectedOrder);

        //when
        Page<Order> result = orderService.findAll(pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent().size() == 1);
        assertThat(result.getContent().getFirst()).isEqualTo(expectedOrder);
        verify(orderRepository).findAll(pageable);
        verify(orderMapper).fromModel(expectedOrderEntity);
    }

    @Test
    @DisplayName("Должен вернуть все заказы для username")
    void findByUsername_ShouldReturnOrdersForUser() {
        //given
        String expectedName = expectedUser.getUsername();
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> entityPage = new PageImpl<>(List.of(expectedOrderEntity), pageable, 1);

        when(userRepository.findByUsername(expectedName)).thenReturn(Optional.of(expectedUserEntity));
        when(orderRepository.findByUserId(expectedUserId, pageable)).thenReturn(entityPage);
        when(orderMapper.fromModel(expectedOrderEntity)).thenReturn(expectedOrder);

        //when
        Page<Order> result = orderService.findByUsername(expectedName, pageable);

        //then
        assertThat(result.getContent()).containsExactly(expectedOrder);
        verify(userRepository).findByUsername(expectedName);
        verify(orderRepository).findByUserId(expectedUserId, pageable);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если user с username не найден")
    void findByUsername_WhenUserNotFound_ShouldThrowException() {
        //given
        String wrongUsername = "unknown";
        when(userRepository.findByUsername(wrongUsername)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> orderService.findByUsername(wrongUsername, Pageable.unpaged()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with username \"unknown\" not found");
    }

    @Test
    @DisplayName("Должен изменить статус заказа")
    void changeStatus_ShouldUpdateOrderStatusAndSave() {
        //given
        String newStatus = "IN_PROGRESS";

        when(orderRepository.findById(expectedOrderId)).thenReturn(Optional.of(expectedOrderEntity));
        when(orderMapper.fromModel(expectedOrderEntity)).thenReturn(expectedOrder);
        when(orderMapper.toModel(any(Order.class), any(UserEntity.class))).thenReturn(expectedOrderEntity);
        when(orderRepository.save(expectedOrderEntity)).thenReturn(expectedOrderEntity);

        //when
        Order result = orderService.changeStatus(expectedOrderId, newStatus);

        //then
        assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
        verify(orderRepository).findById(expectedOrderId);
        verify(orderMapper).fromModel(expectedOrderEntity);
        verify(orderMapper).toModel(expectedOrder, expectedUserEntity);
        verify(orderRepository).save(expectedOrderEntity);
    }

    @Test
    @DisplayName("Должен выбросить исключение для изменения статуса несуществующего заказа")
    void changeStatus_WhenOrderNotFound_ShouldThrowException() {
        //given
        when(orderRepository.findById(expectedOrderId)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> orderService.changeStatus(expectedOrderId, "IN_PROGRESS"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with id");
    }

    @Test
    @DisplayName("Должен удалить заказ через пользователя с ролью admin")
    void deleteOrder_AsAdmin_ShouldDeleteSuccessfully() throws AccessDeniedException {
        //given
        User adminUser = UserFactory.createUser(expectedUserId, "admin", "encoded", "ADMIN", List.of());

        when(orderRepository.findById(expectedOrderId)).thenReturn(Optional.of(expectedOrderEntity));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(userMapper.fromModel(expectedUserEntity)).thenReturn(expectedUser);
        when(userMapper.toModel(any(User.class))).thenReturn(expectedUserEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUserEntity);

        //when
        orderService.deleteOrder(expectedOrderId);

        //then
        verify(orderRepository).findById(expectedOrderId);
        verify(userService).getCurrentUser();
        verify(userMapper).fromModel(expectedUserEntity);
        verify(userMapper).toModel(any(User.class));
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Должен удалить заказ от владельца заказа с ролью User")
    void deleteOrder_AsOwner_ShouldDeleteSuccessfully() throws AccessDeniedException {
        //given
        when(orderRepository.findById(expectedOrderId)).thenReturn(Optional.of(expectedOrderEntity));
        when(userService.getCurrentUser()).thenReturn(expectedUser);
        when(userMapper.fromModel(expectedUserEntity)).thenReturn(expectedUser);
        when(userMapper.toModel(any(User.class))).thenReturn(expectedUserEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(expectedUserEntity);

        //when
        orderService.deleteOrder(expectedOrderId);

        //then
        verify(userService).getCurrentUser();
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении заказа для пользователя без доступа")
    void deleteOrder_AsNonOwnerAndNotAdmin_ShouldThrowAccessDenied() {
        //given
        User otherUser = UserFactory.createUser(UUID.randomUUID(), "other", "encoded", "USER", List.of());

        when(orderRepository.findById(expectedOrderId)).thenReturn(Optional.of(expectedOrderEntity));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        //when then
        assertThatThrownBy(() -> orderService.deleteOrder(expectedOrderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not allowed to delete this order");
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен выбросить исключение при попытке удалить несуществующий заказ")
    void deleteOrder_WhenOrderNotFound_ShouldThrowException() {
        //given
        when(orderRepository.findById(expectedOrderId)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> orderService.deleteOrder(expectedOrderId))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
