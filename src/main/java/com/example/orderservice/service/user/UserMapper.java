package com.example.orderservice.service.user;

import com.example.orderservice.domain.User;
import com.example.orderservice.entity.Role;
import com.example.orderservice.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserEntity toModel(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.id());
        userEntity.setUsername(user.username());
        userEntity.setPassword(user.password());
        userEntity.setRole(Role.valueOf(user.role().toString()));

        return userEntity;
    }

    public User fromModel(UserEntity userEntity) {
        return UserFactory.createUser(userEntity.getId(), userEntity.getUsername(), userEntity.getPassword(), userEntity.getRole().toString());
    }
}
