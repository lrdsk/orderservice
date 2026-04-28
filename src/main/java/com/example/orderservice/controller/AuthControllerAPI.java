package com.example.orderservice.controller;

import com.example.orderservice.dto.AuthRequestDTO;
import com.example.orderservice.dto.AuthResponseDTO;
import com.example.orderservice.dto.RegisterRequestDTO;
import com.example.orderservice.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "Эндпоинты для аутентификации и регистрации")
@RequestMapping("/api/auth")
public interface AuthControllerAPI {

    @PostMapping("/login")
    @Operation(
            summary = "Авторизироваться в системе",
            description = "Аутентификация пользователя по username и password, возвращает JWT-токен"
    )
    AuthResponseDTO login(@RequestBody AuthRequestDTO request);

    @PostMapping("/register")
    @Operation(
            summary = "Зарегистрироваться в системе",
            description = "Создание нового пользователя с ролью USER"
    )
    ResponseEntity<HttpStatus> register(@RequestBody RegisterRequestDTO registerRequestDTO);

    @GetMapping("/me")
    @Operation(
            summary = "Получить информацию о текущем пользователе",
            description = "Возвращает данные пользователя по токену"
    )
    UserDTO getInformation(@AuthenticationPrincipal UserDetails userDetails);
}