package com.example.orderservice.controller;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.UserDTO;
import com.example.orderservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping()
    public List<UserDTO> getAllUsers() {
        LOG.info("Getting information about all users");
        List<User> users = userService.findAll();

        return mapToUserDTO(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUserById(@PathVariable("id") UUID id) {
        LOG.info("Try to delete user with id: {}", id);
        userService.delete(id);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    private List<UserDTO> mapToUserDTO(List<User> users) {
        return users.stream()
                .map(user -> new UserDTO(
                        user.id(),
                        user.username(),
                        user.role().toString()))
                .toList();
    }
}
