package com.example.orderservice.controller;

import com.example.orderservice.domain.Order;
import com.example.orderservice.dto.AdminOrderResponseDTO;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.dto.OrderStatusRequestDTO;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderControllerAPI {
    private final UserService userService;
    private final OrderService orderService;

    @Override
    public ResponseEntity<HttpStatus> createOrder(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OrderRequestDTO orderRequestDTO) {
        userService.addNewOrder(userDetails.getUsername(), orderRequestDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public Page<OrderResponseDTO> getAllOrdersForCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Order> orders = orderService.findByUsername(userDetails.getUsername(), pageable);
        return orders.map(this::mapToOrderResponseDTO);
    }

    @Override
    public Page<AdminOrderResponseDTO> getAllOrders(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.findAll(pageable).map(this::mapToAdminOrderResponseDTO);
    }

    @Override
    public AdminOrderResponseDTO changeOrderStatus(@PathVariable("id") UUID orderId, @RequestBody OrderStatusRequestDTO orderStatusRequestDTO) {
        Order order = orderService.changeStatus(orderId, orderStatusRequestDTO.status());

        return mapToAdminOrderResponseDTO(order);
    }

    @Override
    public ResponseEntity<HttpStatus> deleteOrderById(@PathVariable("id") UUID orderId) throws AccessDeniedException {
        orderService.deleteOrder(orderId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getDescription(),
                order.getStatus().toString(),
                order.getCreatedAt()
        );
    }

    private AdminOrderResponseDTO mapToAdminOrderResponseDTO(Order order) {
        return new AdminOrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getDescription(),
                order.getStatus().toString(),
                order.getCreatedAt()
        );
    }
}
