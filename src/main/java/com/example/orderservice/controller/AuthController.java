package com.example.orderservice.controller;

import com.example.orderservice.domain.User;
import com.example.orderservice.dto.AuthRequestDTO;
import com.example.orderservice.dto.AuthResponseDTO;
import com.example.orderservice.dto.RegisterRequestDTO;
import com.example.orderservice.dto.UserDTO;
import com.example.orderservice.service.AuthService;
import com.example.orderservice.service.UserService;
import com.example.orderservice.service.auth.JWTUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequiredArgsConstructor
public class AuthController implements AuthControllerAPI{

    private final AuthService authService;
    private final UserService userService;

    @Override
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request) {
        return authService.auth(request.username(), request.password());
    }

    @Override
    public ResponseEntity<HttpStatus> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        userService.register(registerRequestDTO);

        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @Override
    public UserDTO getInformation(@AuthenticationPrincipal UserDetails userDetails) {
        User userInformation = userService.findInformationByUsername(userDetails.getUsername());

        return new UserDTO(
                userInformation.getId(),
                userInformation.getUsername(),
                userInformation.getRole().toString()
        );
    }
}