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
@RequiredArgsConstructor
public class UserController implements UserControllerAPI {
    private final UserService userService;

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userService.findAll();

        return mapToUserDTO(users);
    }

    @Override
    public ResponseEntity<HttpStatus> deleteUserById(@PathVariable("id") UUID id) {
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
