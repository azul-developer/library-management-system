package com.liz.library.bootstrap.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liz.library.application.dto.CreateUserRequest;
import com.liz.library.application.factory.UserFactory;
import com.liz.library.bootstrap.AbstractDataSeeder;
import com.liz.library.bootstrap.SeedType;
import com.liz.library.domain.repository.UserRepository;
import com.liz.library.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDataSeeder extends AbstractDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(UserDataSeeder.class);

    private final UserFactory userFactory;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SeedType type() {
        return SeedType.USER;
    }

    @Override
    @Transactional
    public void seed() throws Exception {
        ClassPathResource resource = new ClassPathResource("seed/users.json");
        if (!resource.exists()) {
            log.warn("User seed file not found: seed/users.json");
            return;
        }

        List<UserSeed> seeds;
        try (InputStream is = resource.getInputStream()) {
            seeds = objectMapper.readValue(is, new TypeReference<List<UserSeed>>() {});
        }

        for (UserSeed s : seeds) {
            if (s.email() == null || s.email().isBlank()) {
                log.warn("Skipping user with missing email");
                continue;
            }

            Email email = Email.of(s.email());
            if (userRepository.findByEmail(email).isPresent()) {
                log.info("Seed: user {} exists — skipping", s.email());
                continue;
            }

            CreateUserRequest req = new CreateUserRequest();
            req.setFirstName(s.firstName());
            req.setLastName(s.lastName());
            req.setEmail(s.email());
            req.setPassword(s.password());
            req.setRoleName(s.roleName());

            var user = userFactory.create(req);
            userRepository.save(user);
            log.info("Seed: created user {} ({})", s.firstName(), s.email());
        }
    }

    private record UserSeed(String firstName, String lastName, String email, String password, String roleName) {}
}
