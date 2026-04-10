package com.example.orderservice.controller;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.UserDTO;
import com.example.orderservice.service.UserService;
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
public class UserController {
    private final UserService userService;

    @GetMapping()
    public List<UserDTO> getAllUsers() {
        log.info("Getting information about all users");
        List<User> users = userService.findAll();

        return mapToUserDTO(users);
    }

    @DeleteMapping("/{id}")
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
