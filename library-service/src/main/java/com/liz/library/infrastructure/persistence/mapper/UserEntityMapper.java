package com.liz.library.infrastructure.persistence.mapper;

import com.liz.library.domain.model.Role;
import com.liz.library.domain.model.User;
import com.liz.library.domain.valueobject.Email;
import com.liz.library.infrastructure.persistence.entity.UserEntity;

import java.time.LocalDateTime;

public final class UserEntityMapper {

    private UserEntityMapper() {}

    public static User toDomain(UserEntity e, Role role) {
        return User.restore(
                e.getId(),
                e.getFirstName(),
                e.getLastName(),
                Email.of(e.getEmail()),
                e.getPassword(),
                role
        );
    }

    public static UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail().toString())
                .password(u.getPasswordHash())
                .roleId(u.getRole().getId())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
