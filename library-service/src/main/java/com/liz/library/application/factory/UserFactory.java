package com.liz.library.application.factory;

import com.liz.library.application.dto.CreateUserRequest;
import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import com.liz.library.domain.model.Role;
import com.liz.library.domain.model.User;
import com.liz.library.domain.repository.RoleRepository;
import com.liz.library.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public User create(CreateUserRequest request) {

        Email email = Email.of(request.getEmail());

        String passwordHash = passwordEncoder.encode(request.getPassword());

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() ->    new BusinessException(MessageCodes.ROLE_NOT_FOUND));

        return User.create(
                request.getFirstName(),
                request.getLastName(),
                email,
                passwordHash,
                role
        );
    }
}
