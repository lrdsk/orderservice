package com.example.orderservice.controller;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.User;
import com.example.orderservice.dto.AdminOrderResponseDTO;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.dto.OrderStatusRequestDTO;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final UserService userService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<HttpStatus> createOrder(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OrderRequestDTO orderRequestDTO) {
        log.info("Try to add new order with description: {}, for user with username: {}", orderRequestDTO.description(), userDetails.getUsername());

        userService.addNewOrder(userDetails.getUsername(), orderRequestDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public List<OrderResponseDTO> getAllOrdersForCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Try to get all orders for user with username: {}", userDetails.getUsername());

        User user = userService.findInformationByUsername(userDetails.getUsername());
        return mapToOrderResponseDTOs(user.getOrders());
    }

    @GetMapping("/all")
    public List<AdminOrderResponseDTO> getAllOrders() {
        log.info("Try to get all orders");
        return mapToAdminOrderResponseDTOs(orderService.findAll());
    }

    @PutMapping("/{id}")
    public AdminOrderResponseDTO changeOrderStatus(@PathVariable("id") UUID orderId, @RequestBody OrderStatusRequestDTO orderStatusRequestDTO) {
        log.info("Try to change status to: {} for order with id: {}", orderStatusRequestDTO.status(), orderId);
        Order order = orderService.changeStatus(orderId, orderStatusRequestDTO.status());

        return mapToAdminOrderResponseDTO(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteOrderById(@PathVariable("id") UUID orderId) throws AccessDeniedException {
        log.info("Try to delete order with id: {}", orderId);
        orderService.deleteOrder(orderId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private static List<OrderResponseDTO> mapToOrderResponseDTOs(List<Order> orders) {
        return orders.stream()
                .map(order -> new OrderResponseDTO(
                        order.getDescription(),
                        order.getStatus().toString(),
                        order.getCreatedAt()
                ))
                .toList();
    }

    private static List<AdminOrderResponseDTO> mapToAdminOrderResponseDTOs(List<Order> orders) {
        return orders.stream()
                .map(order -> new AdminOrderResponseDTO(
                        order.getId(),
                        order.getUserId(),
                        order.getDescription(),
                        order.getStatus().toString(),
                        order.getCreatedAt()
                ))
                .toList();
    }

    private static AdminOrderResponseDTO mapToAdminOrderResponseDTO(Order order) {
        return new AdminOrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getDescription(),
                order.getStatus().toString(),
                order.getCreatedAt()
        );
    }
}
