package com.example.orderservice.service.user;

import com.example.orderservice.domain.User;
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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public User registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username '" + request.username() + "' is already taken");
        }

        User user = UserFactory.createUser(request.username(), passwordEncoder.encode(request.password()), "USER");

        UserEntity userEntity = userMapper.toModel(user);
        userRepository.save(userEntity);

        return user;
    }

    @Override
    public User getUserInformation(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return userMapper.fromModel(userEntity);
    }
}
