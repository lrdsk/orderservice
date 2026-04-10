package com.example.orderservice.service.user;

import com.example.orderservice.domain.User;
import com.example.orderservice.domain.UserFactory;
import com.example.orderservice.entity.Role;
import com.example.orderservice.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final OrderMapper orderMapper;
    public UserEntity toModel(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.getId());
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        userEntity.setRole(Role.valueOf(user.getRole().toString()));
        userEntity.setOrders(
                user.getOrders()
                        .stream()
                        .map(order -> orderMapper.toModel(order, userEntity))
                        .toList());


        return userEntity;
    }

    public User fromModel(UserEntity userEntity) {
        return UserFactory.createUser(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getRole().toString(),
                userEntity.getOrders()
                        .stream()
                        .map(orderMapper::fromModel)
                        .toList()
        );
    }
}
