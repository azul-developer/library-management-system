package com.liz.library.application.service.impl;

import com.liz.library.application.dto.LoginRequest;
import com.liz.library.application.dto.LoginResponse;
import com.liz.library.application.service.AuthService;
import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.exception.InvalidCredentialsException;
import com.liz.library.domain.message.MessageCodes;
import com.liz.library.domain.model.User;
import com.liz.library.domain.repository.UserRepository;
import com.liz.library.domain.valueobject.Email;
import com.liz.library.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(Email.of(request.getEmail()))
                .orElseThrow(InvalidCredentialsException::new);

        boolean valid = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!valid) {
            throw new BusinessException(MessageCodes.INVALID_CREDENTIALS);
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token);
    }
}