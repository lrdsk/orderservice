package com.example.orderservice.service.auth;

import com.example.orderservice.dto.AuthResponseDTO;
import com.example.orderservice.service.AuthService;
import com.example.orderservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Реализация сервиса аутентификации.
 * <p>
 * Отвечает за проверку учётных данных и выдачу JWT-токена при успешном входе.
 * Использует {@link AuthenticationManager} Spring Security для валидации пары логин/пароль
 * и {@link JWTUtils} для генерации токена.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtService;

    /**
     * Выполняет аутентификацию пользователя по логину и паролю.
     * <p>
     * При успешной аутентификации создаёт JWT-токен и возвращает его в ответе.
     * В случае неверных учётных данных выбрасывается исключение Spring Security.
     * </p>
     *
     * @param username логин пользователя
     * @param password пароль (в открытом виде)
     * @return DTO {@link AuthResponseDTO} с сгенерированным JWT-токеном
     */
    @Override
    public AuthResponseDTO auth(String username, String password) {
        log.info("Authenticating user: {}", username);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        log.info("Authenticate user has been successfully");

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(Objects.requireNonNull(userDetails));
        return new AuthResponseDTO(token);
    }
}
