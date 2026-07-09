package com.liz.library.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liz.library.application.dto.CreateUserRequest;
import com.liz.library.application.dto.UpdateUserRequest;
import com.liz.library.application.dto.UserResponse;
import com.liz.library.application.factory.UserFactory;
import com.liz.library.application.mapper.UserMapper;
import com.liz.library.application.service.impl.UserServiceImpl;
import com.liz.library.infrastructure.config.SecurityConfig;
import com.liz.library.infrastructure.security.JwtAuthenticationFilter;
import com.liz.library.infrastructure.security.JwtService;
import com.liz.library.domain.model.Role;
import com.liz.library.domain.model.User;
import com.liz.library.domain.repository.UserRepository;
import com.liz.library.domain.valueobject.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = UserController.class)
@org.springframework.context.annotation.Import({SecurityConfig.class, JwtAuthenticationFilter.class, UserServiceImpl.class})
class UserControllerAccessTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserFactory userFactory;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void adminCanCreateUser() throws Exception {
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        User domainUser = sampleDomainUser(UUID.randomUUID(), "USER");
        when(userFactory.create(any())).thenReturn(domainUser);
        when(userRepository.findByEmail(any())).thenReturn(java.util.Optional.empty());
        when(userRepository.save(domainUser)).thenReturn(domainUser);
        when(userMapper.toResponse(domainUser)).thenReturn(sampleUserResponse());

        mockMvc.perform(post("/api/users")
                .with(csrf())
            .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateUserRequest())))
                .andExpect(status().isCreated());

        verify(userFactory).create(any());
    }

    @Test
    void adminCanListUsers() throws Exception {
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        User domainUser = sampleDomainUser(UUID.randomUUID(), "USER");
        when(userRepository.findAll()).thenReturn(List.of(domainUser));
        when(userMapper.toResponse(domainUser)).thenReturn(sampleUserResponse());

        mockMvc.perform(get("/api/users")
                .header("Authorization", bearer(ADMIN_TOKEN)))
                .andExpect(status().isOk());

        verify(userRepository).findAll();
    }

    @Test
    void adminCanGetUser() throws Exception {
        UUID userId = UUID.randomUUID();
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        User domainUser = sampleDomainUser(userId, "USER");
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(domainUser));
        when(userMapper.toResponse(domainUser)).thenReturn(sampleUserResponse());

        mockMvc.perform(get("/api/users/{id}", userId)
                .header("Authorization", bearer(ADMIN_TOKEN)))
                .andExpect(status().isOk());

        verify(userRepository).findById(userId);
    }

    @Test
    void adminCanUpdateUser() throws Exception {
        UUID userId = UUID.randomUUID();
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        User existingUser = sampleDomainUser(userId, "USER");
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(sampleUserResponse());

        mockMvc.perform(put("/api/users/{id}", userId)
                .with(csrf())
            .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateUserRequest())))
                .andExpect(status().isOk());

        verify(userRepository).findById(userId);
    }

    @Test
    void adminCanPatchUser() throws Exception {
        UUID userId = UUID.randomUUID();
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        User existingUser = sampleDomainUser(userId, "USER");
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(sampleUserResponse());

        mockMvc.perform(patch("/api/users/{id}", userId)
                .with(csrf())
            .header("Authorization", bearer(ADMIN_TOKEN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateUserRequest())))
                .andExpect(status().isOk());

        verify(userRepository).findById(userId);
    }

    @Test
    void adminCanDeleteUser() throws Exception {
        UUID userId = UUID.randomUUID();
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        User existingUser = sampleDomainUser(userId, "USER");
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf())
                        .header("Authorization", bearer(ADMIN_TOKEN)))
                .andExpect(status().isNoContent());

        verify(userRepository).delete(existingUser);
    }

    @Test
    void userCannotAccessUsersEndpoints() throws Exception {
        setupAuthentication(USER_TOKEN, UUID.randomUUID(), "USER");
        mockMvc.perform(get("/api/users")
                        .header("Authorization", bearer(USER_TOKEN)))
                .andExpect(status().isForbidden());
    }

    private void setupAuthentication(String token, UUID userId, String roleName) {
        com.liz.library.domain.model.User user = com.liz.library.domain.model.User.restore(
                userId,
                "Test",
                "User",
                com.liz.library.domain.valueobject.Email.of("test@library.com"),
                null,
                com.liz.library.domain.model.Role.builder().name(roleName).build());

        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(jwtService.isTokenValid(token, user)).thenReturn(true);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private User sampleDomainUser(UUID id, String roleName) {
        return User.restore(
                id,
                "Test",
                "User",
                Email.of("test@library.com"),
                null,
                Role.builder().name(roleName).build());
    }

    private CreateUserRequest validCreateUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@test.com");
        request.setPassword("MySecurePassword123");
        request.setRoleName("USER");
        return request;
    }

    private UpdateUserRequest validUpdateUserRequest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe@test.com");
        request.setPassword("AnotherSecurePassword123");
        request.setRoleName("ADMIN");
        return request;
    }

    private UserResponse sampleUserResponse() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .roleName("USER")
                .build();
    }
}
