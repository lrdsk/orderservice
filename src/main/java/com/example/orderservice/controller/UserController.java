package com.example.orderservice.controller;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.UserDTO;
import com.example.orderservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User API")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Получить информацию о всех пользователях",
            description = "Возвращает список с информацией о всех пользователях системы"
    )
    public List<UserDTO> getAllUsers() {
        log.info("Getting information about all users");
        List<User> users = userService.findAll();

        return mapToUserDTO(users);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пользователя по его id"
    )
    public ResponseEntity<HttpStatus> deleteUserById(@PathVariable("id") UUID id) {
        log.info("Try to delete user with id: {}", id);
        userService.delete(id);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    private List<UserDTO> mapToUserDTO(List<User> users) {
        return users.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().toString()))
                .toList();
    }
}
