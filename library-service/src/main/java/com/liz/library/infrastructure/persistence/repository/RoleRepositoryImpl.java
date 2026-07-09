package com.liz.library.infrastructure.persistence.repository;

import com.liz.library.domain.model.Role;
import com.liz.library.domain.repository.RoleRepository;
import com.liz.library.infrastructure.persistence.entity.RoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;

    @Override
    public Role save(Role role) {
        RoleEntity entity = RoleEntity.builder()
                .id(role.getId())
                .name(role.getName())
                .build();

        RoleEntity saved = roleJpaRepository.save(entity);

        return Role.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return roleJpaRepository.findById(id)
                .map(e -> Role.builder().id(e.getId()).name(e.getName()).build());
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name)
                .map(e -> Role.builder().id(e.getId()).name(e.getName()).build());
    }
}
