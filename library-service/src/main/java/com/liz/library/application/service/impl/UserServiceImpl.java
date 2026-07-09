package com.liz.library.application.service.impl;

import com.liz.library.application.dto.CreateUserRequest;
import com.liz.library.application.dto.UpdateUserRequest;
import com.liz.library.application.dto.UserResponse;
import com.liz.library.application.factory.UserFactory;
import com.liz.library.application.mapper.UserMapper;
import com.liz.library.application.service.UserService;
import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import com.liz.library.domain.model.User;
import com.liz.library.domain.repository.UserRepository;
import com.liz.library.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserFactory userFactory;
    private final UserMapper userMapper;

    private User loadUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() ->new BusinessException(MessageCodes.USER_NOT_FOUND));
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        User user = userFactory.create(request);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BusinessException(MessageCodes.EMAIL_ALREADY_EXISTS);
        }

        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }

    @Override
    public List<UserResponse> findAll() {
       return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getById(UUID id) {
        User user = loadUser(id);
        return userMapper.toResponse(user);
        }


    @Override
    public UserResponse update(UUID id, CreateUserRequest request) {
        User existing = loadUser(id);

        // build updated user; keep password if not provided
        User updated = User.restore(
                existing.getId(),
                request.getFirstName(),
                request.getLastName(),
                Email.of(request.getEmail()),
                existing.getPasswordHash(),
                existing.getRole()
        );

        User saved = userRepository.save(updated);

        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse partialUpdate(UUID id, UpdateUserRequest request) {
        User existing = loadUser(id);

        String firstName = request.getFirstName() != null ? request.getFirstName() : existing.getFirstName();
        String lastName = request.getLastName() != null ? request.getLastName() : existing.getLastName();
        Email email = request.getEmail() != null ? Email.of(request.getEmail()) : existing.getEmail();
        String passwordHash = request.getPassword() != null ? existing.getPasswordHash() : existing.getPasswordHash();

        User updated = User.restore(
                existing.getId(),
                firstName,
                lastName,
                email,
                passwordHash,
                existing.getRole()
        );

        User saved = userRepository.save(updated);

        return userMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        User user = loadUser(id);
        userRepository.delete(user);
    }
}
