package com.liz.library.bootstrap.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liz.library.bootstrap.AbstractDataSeeder;
import com.liz.library.bootstrap.SeedType;
import com.liz.library.domain.model.Role;
import com.liz.library.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleDataSeeder extends AbstractDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(RoleDataSeeder.class);

    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SeedType type() {
        return SeedType.ROLE;
    }

    @Override
    @Transactional
    public void seed() throws Exception {
        ClassPathResource resource = new ClassPathResource("seed/roles.json");
        if (!resource.exists()) {
            log.warn("Role seed file not found: seed/roles.json");
            return;
        }

        List<RoleSeed> seeds;
        try (InputStream is = resource.getInputStream()) {
            seeds = objectMapper.readValue(is, new TypeReference<List<RoleSeed>>() {});
        }

        for (RoleSeed s : seeds) {
            if (s.name() == null || s.name().isBlank()) {
                log.warn("Skipping empty role name");
                continue;
            }
            if (roleRepository.findByName(s.name()).isPresent()) {
                log.info("Seed: role {} exists — skipping", s.name());
                continue;
            }

            Role role = Role.builder().id(UUID.randomUUID()).name(s.name()).build();
            roleRepository.save(role);
            log.info("Seed: created role {}", s.name());
        }
    }

    private record RoleSeed(String name) {}
}
