package com.liz.library.infrastructure.persistence.repository;

import com.liz.library.domain.model.User;
import com.liz.library.domain.repository.RoleRepository;
import com.liz.library.domain.repository.UserRepository;
import com.liz.library.domain.valueobject.Email;
import com.liz.library.infrastructure.persistence.entity.UserEntity;
import com.liz.library.infrastructure.persistence.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final RoleRepository roleRepository;

    @Override
    public User save(User user) {

        UserEntity entity = UserEntityMapper.toEntity(user);

        return userJpaRepository.findById(entity.getId())
                .map(managed -> {
                    managed.setFirstName(entity.getFirstName());
                    managed.setLastName(entity.getLastName());
                    managed.setEmail(entity.getEmail());
                    managed.setPassword(entity.getPassword());
                    managed.setRoleId(entity.getRoleId());
                    UserEntity saved = userJpaRepository.save(managed);
                    return UserEntityMapper.toDomain(saved, roleRepository.findById(saved.getRoleId()).orElse(null));
                })
                .orElseGet(() -> {
                    UserEntity saved = userJpaRepository.save(entity);
                    return UserEntityMapper.toDomain(saved, roleRepository.findById(saved.getRoleId()).orElse(null));
                });
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id)
                .map(e -> UserEntityMapper.toDomain(e, roleRepository.findById(e.getRoleId()).orElse(null)));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.toString())
                .map(e -> UserEntityMapper.toDomain(e, roleRepository.findById(e.getRoleId()).orElse(null)));
    }

    @Override
    public void delete(User user) {
        // Use deleteById to avoid issues with detached / partially-populated entities
        // (e.g., missing role or version mismatches). This performs a direct delete
        // by identifier at the JPA level.
        userJpaRepository.deleteById(user.getId());
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll()
                .stream()
                .map(e -> UserEntityMapper.toDomain(e, roleRepository.findById(e.getRoleId()).orElse(null)))
                .toList();
    }
}
