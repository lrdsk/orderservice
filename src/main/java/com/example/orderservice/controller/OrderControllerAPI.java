package com.example.orderservice.controller;

import com.example.orderservice.dto.AdminOrderResponseDTO;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.dto.OrderStatusRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Order API", description = "Управление заказами")
@RequestMapping("/api/orders")
public interface OrderControllerAPI {

    @PostMapping
    @Operation(
            summary = "Создание нового заказа",
            description = "Создание нового заказа доступно только авторизованным пользователям"
    )
    ResponseEntity<HttpStatus> createOrder(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody OrderRequestDTO orderRequestDTO);

    @GetMapping
    @Operation(
            summary = "Получить все заказы для текущего пользователя",
            description = "Возвращает список заказов для авторизированного пользователя"
    )
    Page<OrderResponseDTO> getAllOrdersForCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);

    @GetMapping("/all")
    @Operation(
            summary = "Получить список всех заказов",
            description = "Возвращает информацию о всех заказах для всех пользователей, доступно для администраторов"
    )
    Page<AdminOrderResponseDTO> getAllOrders(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);

    @PutMapping("/{id}")
    @Operation(
            summary = "Изменить статус заказа",
            description = "Изменение статуса заказа по его id, доступно для администраторов"
    )
    AdminOrderResponseDTO changeOrderStatus(@PathVariable("id") UUID orderId,
                                            @RequestBody OrderStatusRequestDTO orderStatusRequestDTO);

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить заказ по его id",
            description = "Удаление заказа из списка пользователя, доступно владельцу заказа и администраторам"
    )
    ResponseEntity<HttpStatus> deleteOrderById(@PathVariable("id") UUID orderId) throws AccessDeniedException;
}