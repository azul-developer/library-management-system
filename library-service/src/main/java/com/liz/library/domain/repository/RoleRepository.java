package com.liz.library.domain.repository;

import com.liz.library.domain.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(UUID id);

    Optional<Role> findByName(String name);
}