package com.liz.library.application.service;

import com.liz.library.application.dto.CreateUserRequest;
import com.liz.library.application.dto.UpdateUserRequest;
import com.liz.library.application.dto.UserResponse;
import com.liz.library.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    List<UserResponse> findAll();

    UserResponse getById(UUID id);

    UserResponse update(UUID id, CreateUserRequest request);

    UserResponse partialUpdate(UUID id, UpdateUserRequest request);

    void delete(UUID id);
}
