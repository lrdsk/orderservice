package com.example.orderservice.service;

import com.example.orderservice.domain.User;
import com.example.orderservice.domain.UserFactory;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.RegisterRequestDTO;
import com.example.orderservice.entity.UserEntity;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.repository.exception.UsernameAlreadyExistsException;
import com.example.orderservice.utils.mapper.UserMapper;
import com.example.orderservice.service.user.UserServiceImpl;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private UserEntity expectedUserEntity;
    private User expectedUser;

    @BeforeEach
    void setUp() {
        expectedUserEntity = getExpectedUserEntity();

        expectedUser = getExpectedUser(expectedUserEntity.getId());
    }

    private @NonNull User getExpectedUser(UUID id) {
        return UserFactory.createUser(id, getExpectedTestName(), getExpectedEncodedPassword(), "USER", List.of());
    }

    private @NonNull UserEntity getExpectedUserEntity() {
        UUID userId = UUID.fromString("ddc5aed9-7c41-49fe-a36e-514e5faa1017");
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername(getExpectedTestName());
        userEntity.setPassword(getExpectedEncodedPassword());
        userEntity.setRole(com.example.orderservice.entity.Role.USER);
        return userEntity;
    }

    private static @NonNull RegisterRequestDTO getRegisterRequest() {
        return new RegisterRequestDTO(getExpectedTestName(), "password");
    }

    private static @NonNull String getExpectedEncodedPassword() {
        return "encoded";
    }

    private static @NonNull String getExpectedTestName() {
        return "Test name";
    }

    @Test
    @DisplayName("Должен зарегистрировать нового пользователя и сохранить в БД")
    void register_ShouldSaveNewUser() {
        //given
        when(userRepository.existsByUsername(getExpectedTestName())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn(getExpectedEncodedPassword());
        when(userMapper.toModel(any(User.class))).thenReturn(expectedUserEntity);
        when(userRepository.save(expectedUserEntity)).thenReturn(expectedUserEntity);

        RegisterRequestDTO registerRequest = getRegisterRequest();

        //when
        User result = userService.register(registerRequest);

        //then
        assertThat(result.getUsername()).isEqualTo(getExpectedTestName());
        verify(userRepository).existsByUsername(getExpectedTestName());
        verify(passwordEncoder).encode("password");
        verify(userMapper).toModel(any(User.class));
        verify(userRepository).save(expectedUserEntity);
    }

    @Test
    @DisplayName("Должен выбросить исключение при попытке зарегистрировать существующего пользователя")
    void register_WhenUsernameExists_ShouldThrowException() {
        //given
        String expectedTestName = getExpectedTestName();
        when(userRepository.existsByUsername(expectedTestName)).thenReturn(true);

        //when then
        assertThatThrownBy(() -> userService.register(getRegisterRequest()))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("Username '%s' is already taken".formatted(expectedTestName));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен вернуть пользователя по его username")
    void findInformationByUsername_ShouldReturnUser() {
        //given
        String expectedTestName = getExpectedTestName();

        when(userRepository.findByUsername(expectedTestName)).thenReturn(Optional.of(expectedUserEntity));
        when(userMapper.fromModel(expectedUserEntity)).thenReturn(expectedUser);

        //when
        User result = userService.findInformationByUsername(expectedTestName);

        //then
        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("Должен выбросить исключение при попытке найти несуществующего пользователя")
    void findInformationByUsername_WhenNotFound_ShouldThrowException() {
        //given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> userService.findInformationByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User with username \"unknown\" not found");
    }

    @Test
    @DisplayName("Должен вернуть список всех пользователей")
    void findAll_ShouldReturnListOfUsers() {
        //given
        when(userRepository.findAll()).thenReturn(List.of(expectedUserEntity));
        when(userMapper.fromModel(expectedUserEntity)).thenReturn(expectedUser);

        //when
        List<User> users = userService.findAll();

        //then
        assertThat(users).hasSize(1).containsExactly(expectedUser);
    }

    @Test
    @DisplayName("Должен удалить пользователя")
    void delete_ShouldCallRepositoryDelete() {
        UUID id = UUID.randomUUID();
        userService.delete(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    @DisplayName("Должен добавить заказ в список заказов пользователя")
    void addNewOrder_ShouldAddOrderAndSaveUser() {
        //given
        String username = getExpectedTestName();
        OrderRequestDTO orderRequest = new OrderRequestDTO("New order");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUserEntity));
        when(userMapper.fromModel(expectedUserEntity)).thenReturn(expectedUser);
        when(userMapper.toModel(any(User.class))).thenReturn(expectedUserEntity);
        when(userRepository.save(expectedUserEntity)).thenReturn(expectedUserEntity);

        //when
        userService.addNewOrder(username, orderRequest);

        //then
        verify(userRepository).findByUsername(username);
        verify(userMapper).fromModel(expectedUserEntity);
        verify(userMapper).toModel(any(User.class));
        verify(userRepository).save(expectedUserEntity);
    }

    @Test
    @DisplayName("Должен выбросить исключение если пользователь не найден по username")
    void addNewOrder_WhenUserNotFound_ShouldThrowException() {
        //given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> userService.addNewOrder("unknown", new OrderRequestDTO("desc")))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Должен вернуть текущего пользователя из security context")
    void getCurrentUser_ShouldReturnUserFromSecurityContext() {
        //given
        //Setup security context
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = getSecurityContextMock();

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(getExpectedTestName());
        when(userRepository.findByUsername(getExpectedTestName())).thenReturn(Optional.of(expectedUserEntity));
        when(userMapper.fromModel(expectedUserEntity)).thenReturn(expectedUser);

        //when
        User result = userService.getCurrentUser();

        //then
        assertThat(result).isEqualTo(expectedUser);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Должен выбросить исключение при попытке найти текущего пользователя без авторизации")
    void getCurrentUser_WhenNotAuthenticated_ShouldThrowException() {
        //given
        SecurityContext securityContext = getSecurityContextMock();
        when(securityContext.getAuthentication()).thenReturn(null);

        //when then
        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not authenticated");
        SecurityContextHolder.clearContext();
    }

    private static @NonNull SecurityContext getSecurityContextMock() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        return securityContext;
    }
}
