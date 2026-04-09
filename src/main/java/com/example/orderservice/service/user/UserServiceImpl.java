package com.example.orderservice.service.user;

import com.example.orderservice.domain.User;
import com.example.orderservice.domain.UserFactory;
import com.example.orderservice.dto.RegisterRequestDTO;
import com.example.orderservice.entity.UserEntity;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.repository.exception.UsernameAlreadyExistsException;
import com.example.orderservice.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public User register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username '" + request.username() + "' is already taken");
        }

        User user = UserFactory.createUser(request.username(), passwordEncoder.encode(request.password()), "USER");

        UserEntity userEntity = userMapper.toModel(user);
        userRepository.save(userEntity);

        return user;
    }

    @Override
    public User getInformationByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username \"%s\" not found".formatted(username)));

        return userMapper.fromModel(userEntity);
    }

    @Override
    public List<User> findAll() {
        List<UserEntity> userEntities = userRepository.findAll();

        return userEntities.stream()
                .map(userMapper::fromModel)
                .toList();
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
