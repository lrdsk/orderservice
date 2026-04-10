package com.example.orderservice.controller;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final static Logger LOG = LoggerFactory.getLogger(OrderController.class);
    private final UserService userService;
    @PostMapping
    public ResponseEntity<HttpStatus> createOrder(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OrderRequestDTO orderRequestDTO) {
        LOG.info("Try to add new order with description: {}, for user with username: {}", orderRequestDTO.description(), userDetails.getUsername());

        userService.addNewOrder(userDetails.getUsername(), orderRequestDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
