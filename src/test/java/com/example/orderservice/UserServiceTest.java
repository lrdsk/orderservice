package com.example.orderservice;

import com.example.orderservice.domain.Role;
import com.example.orderservice.domain.User;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.RegisterRequestDTO;
import com.example.orderservice.entity.UserEntity;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.repository.exception.UsernameAlreadyExistsException;
import com.example.orderservice.service.user.UserMapper;
import com.example.orderservice.service.user.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private UserEntity userEntity;
    private User user;
    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername("john");
        userEntity.setPassword("encoded");
        userEntity.setRole(com.example.orderservice.entity.Role.USER);

        user = new User(userId, "john", "encoded", Role.USER, List.of());
        registerRequest = new RegisterRequestDTO("john", "password");
    }

    @Test
    void register_ShouldSaveNewUser() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userMapper.toModel(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        User result = userService.register(registerRequest);

        assertThat(result.getUsername()).isEqualTo("john");
        verify(userRepository).existsByUsername("john");
        verify(passwordEncoder).encode("password");
        verify(userMapper).toModel(any(User.class));
        verify(userRepository).save(userEntity);
    }

    @Test
    void register_WhenUsernameExists_ShouldThrowException() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("Username 'john' is already taken");
        verify(userRepository, never()).save(any());
    }

    @Test
    void findInformationByUsername_ShouldReturnUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(userEntity));
        when(userMapper.fromModel(userEntity)).thenReturn(user);

        User result = userService.findInformationByUsername("john");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findInformationByUsername_WhenNotFound_ShouldThrowException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findInformationByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with username \"unknown\" not found");
    }

    @Test
    void findAll_ShouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity));
        when(userMapper.fromModel(userEntity)).thenReturn(user);

        List<User> users = userService.findAll();

        assertThat(users).hasSize(1).containsExactly(user);
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        UUID id = UUID.randomUUID();
        userService.delete(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void addNewOrder_ShouldAddOrderAndSaveUser() {
        String username = "john";
        OrderRequestDTO orderRequest = new OrderRequestDTO("New order");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.fromModel(userEntity)).thenReturn(user);
        when(userMapper.toModel(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        userService.addNewOrder(username, orderRequest);

        verify(userRepository).findByUsername(username);
        verify(userMapper).fromModel(userEntity);
        verify(userMapper).toModel(any(User.class));
        verify(userRepository).save(userEntity);
    }

    @Test
    void addNewOrder_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addNewOrder("unknown", new OrderRequestDTO("desc")))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void getCurrentUser_ShouldReturnUserFromSecurityContext() {
        // Setup security context
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(userEntity));
        when(userMapper.fromModel(userEntity)).thenReturn(user);

        User result = userService.getCurrentUser();

        assertThat(result).isEqualTo(user);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ShouldThrowException() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(RuntimeException.class) // или конкретное AuthenticationException
                .hasMessageContaining("Not authenticated");
        SecurityContextHolder.clearContext();
    }
}
