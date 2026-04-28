package com.example.orderservice.service.user;

import com.example.orderservice.domain.*;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.RegisterRequestDTO;
import com.example.orderservice.entity.UserEntity;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.repository.exception.UsernameAlreadyExistsException;
import com.example.orderservice.service.UserService;
import com.example.orderservice.utils.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для управления пользователями.
 * <p>
 * Предоставляет бизнес-логику для регистрации, поиска, удаления пользователей,
 * добавления заказов и получения текущего аутентифицированного пользователя.
 * Использует {@link UserRepository} для доступа к базе данных и {@link UserMapper}
 * для преобразования между сущностями и доменными объектами.
 * </p>
 *
 * @see UserService
 * @see UserRepository
 * @see UserMapper
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * При регистрации проверяется уникальность имени пользователя.
     * Пароль кодируется с использованием {@link PasswordEncoder}.
     * Создаётся пользователь с ролью {@link Role#USER} и пустым списком заказов.
     * </p>
     *
     * @param request DTO с данными для регистрации (username и password)
     * @return созданный доменный объект {@link User}
     * @throws UsernameAlreadyExistsException если пользователь с таким username уже существует
     */
    @Transactional
    public User register(RegisterRequestDTO request) {
        log.info("Registering new user: {}", request.username());
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed - username already exists: {}", request.username());
            throw new UsernameAlreadyExistsException("Username '" + request.username() + "' is already taken");
        }

        User user = UserFactory.createUser(request.username(), passwordEncoder.encode(request.password()), Role.USER, List.of());

        UserEntity userEntity = userMapper.toModel(user);
        userRepository.save(userEntity);
        log.info("User {} successfully registered with id {}", request.username(), userEntity.getId());

        return user;
    }

    /**
     * Находит и возвращает информацию о пользователе по его имени.
     *
     * @param username имя пользователя (логин)
     * @return доменный объект {@link User}
     * @throws UsernameNotFoundException если пользователь с указанным username не найден
     */
    @Override
    public User findInformationByUsername(String username) {
        log.debug("Fetching user information for username: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username \"%s\" not found".formatted(username)));

        return userMapper.fromModel(userEntity);
    }

    /**
     * Возвращает список всех пользователей системы.
     *
     * @return список доменных объектов {@link User}
     */
    @Override
    public List<User> findAll() {
        log.debug("Fetching all users");
        List<UserEntity> userEntities = userRepository.findAll();
        log.debug("Found {} users", userEntities.size());

        return userEntities.stream()
                .map(userMapper::fromModel)
                .toList();
    }

    /**
     * Удаляет пользователя по его уникальному идентификатору.
     * <p>
     * При удалении пользователя также удаляются все его заказы (каскадно, в зависимости от настроек JPA).
     * </p>
     *
     * @param id идентификатор пользователя (UUID)
     */
    @Transactional
    @Override
    public void delete(UUID id) {
        log.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
        log.info("User {} deleted", id);
    }

    /**
     * Добавляет новый заказ для указанного пользователя.
     * <p>
     * Создаётся заказ через {@link OrderFactory} и связывается с пользователем.
     * Изменённый пользователь (с новым заказом) сохраняется в репозитории.
     * </p>
     *
     * @param username         имя пользователя, которому добавляется заказ
     * @param orderRequestDTO  DTO с описанием заказа
     * @throws UsernameNotFoundException если пользователь с таким username не найден
     */
    @Transactional
    @Override
    public void addNewOrder(String username, OrderRequestDTO orderRequestDTO) {
        log.info("Adding new order for user: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username \"%s\" not found".formatted(username)));

        User user = userMapper.fromModel(userEntity);
        Order order = OrderFactory.createOrder(orderRequestDTO.description(), user.getId());

        user.addOrder(order);

        UserEntity updatedUserEntity = userMapper.toModel(user);

        userRepository.save(updatedUserEntity);
        log.info("Order added successfully for user: {}", username);
    }

    /**
     * Возвращает текущего аутентифицированного пользователя.
     * <p>
     * Информация о пользователе извлекается из контекста безопасности Spring Security.
     * Если аутентификация отсутствует или пользователь не найден в БД, выбрасывается исключение.
     * </p>
     *
     * @return доменный объект {@link User} текущего пользователя
     * @throws AuthenticationException   если пользователь не аутентифицирован
     * @throws UsernameNotFoundException если аутентифицированный пользователь не найден в базе данных
     */
    @Override
    public User getCurrentUser() {
        log.debug("Retrieving current authenticated user");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationException("Not authenticated") {};
        }
        String username = auth.getName();
        log.debug("Current user: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::fromModel)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
