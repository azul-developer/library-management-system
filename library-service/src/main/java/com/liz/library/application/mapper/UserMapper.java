package com.liz.library.application.mapper;

import com.liz.library.application.dto.UserResponse;
import com.liz.library.domain.model.Role;
import com.liz.library.domain.model.User;
import com.liz.library.domain.valueobject.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleName", source = "role")
    @Mapping(target = "email", source = "email")
    UserResponse toResponse(User user);

    default String map(Role role) {
        return role != null ? role.getName() : null;
    }

    default String map(Email email) {
        return email != null ? email.toString() : null;
    }
}