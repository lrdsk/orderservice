package com.example.orderservice.controller;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.AuthRequestDTO;
import com.example.orderservice.dto.AuthResponseDTO;
import com.example.orderservice.dto.RegisterRequestDTO;
import com.example.orderservice.dto.UserDTO;
import com.example.orderservice.service.UserService;
import com.example.orderservice.service.auth.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request) {
        log.info("Try to login with username: {}", request.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        log.info("user with username {} has been successfully authenticated", request.username());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(Objects.requireNonNull(userDetails));
        return new AuthResponseDTO(token);
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("Try to register new user with username: {}", registerRequestDTO.username());
        userService.register(registerRequestDTO);
        log.info("The new user has been successfully registered");

        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public UserDTO getInformation(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Try to get information for user with username: {}", userDetails.getUsername());
        User userInformation = userService.findInformationByUsername(userDetails.getUsername());

        return new UserDTO(
                userInformation.getId(),
                userInformation.getUsername(),
                userInformation.getRole().toString()
        );
    }
}