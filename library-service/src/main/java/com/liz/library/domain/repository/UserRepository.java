package com.liz.library.domain.repository;

import com.liz.library.domain.model.User;
import com.liz.library.domain.valueobject.Email;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    List<User> findAll();

    void delete(User user);
}