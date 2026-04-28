package com.example.orderservice.controller;

import com.example.orderservice.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User API", description = "Управление пользователями")
@RequestMapping("/api/users")
public interface UserControllerAPI {

    @GetMapping
    @Operation(
            summary = "Получить информацию о всех пользователях",
            description = "Возвращает список с информацией о всех пользователях системы"
    )
    List<UserDTO> getAllUsers();

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пользователя по его id",
            description = "Удаляет пользователя и связанные с ним заказы (требует прав администратора)"
    )
    ResponseEntity<HttpStatus> deleteUserById(@PathVariable("id") UUID id);
}