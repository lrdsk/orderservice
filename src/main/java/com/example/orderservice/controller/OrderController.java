package com.example.orderservice.controller;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.User;
import com.example.orderservice.dto.AdminOrderResponseDTO;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final static Logger LOG = LoggerFactory.getLogger(OrderController.class);
    private final UserService userService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<HttpStatus> createOrder(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OrderRequestDTO orderRequestDTO) {
        LOG.info("Try to add new order with description: {}, for user with username: {}", orderRequestDTO.description(), userDetails.getUsername());

        userService.addNewOrder(userDetails.getUsername(), orderRequestDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public List<OrderResponseDTO> getAllOrdersForCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        LOG.info("Try to get all orders for user with username: {}", userDetails.getUsername());

        User user = userService.findInformationByUsername(userDetails.getUsername());
        return mapToOrderResponseDTOs(user.getOrders());
    }

    @GetMapping("/all")
    public List<AdminOrderResponseDTO> getAllOrders() {
        LOG.info("Try to get all orders");
        return mapToAdminOrderResponseDTOs(orderService.findAll());
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
                        order.getUserId(),
                        order.getDescription(),
                        order.getStatus().toString(),
                        order.getCreatedAt()
                ))
                .toList();
    }
}
