package com.liz.library.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liz.library.application.service.AuthService;
import com.liz.library.application.service.BookService;
import com.liz.library.application.service.UserService;
import com.liz.library.domain.model.Role;
import com.liz.library.domain.model.User;
import com.liz.library.domain.repository.UserRepository;
import com.liz.library.domain.valueobject.Email;
import com.liz.library.infrastructure.config.SecurityConfig;
import com.liz.library.infrastructure.security.JwtAuthenticationFilter;
import com.liz.library.infrastructure.security.JwtService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, BookController.class, UserController.class})
@org.springframework.context.annotation.Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityEndpointsParameterizedTest {

    private static final String USER_TOKEN = "user-token";
    private static final String ADMIN_TOKEN = "admin-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private UserService userService;

    record EndpointSecurityCase(
            HttpMethod method,
            String url,
            int expectedWithoutToken,
            int expectedUser,
            int expectedAdmin
    ) {}

    static Stream<EndpointSecurityCase> securityCases() {
        return Stream.of(
                new EndpointSecurityCase(HttpMethod.POST, "/auth/login", 200, 200, 200),
                new EndpointSecurityCase(HttpMethod.GET, "/api/books", 403, 200, 200),
                new EndpointSecurityCase(HttpMethod.GET, "/api/books/{id}", 403, 200, 200),
                new EndpointSecurityCase(HttpMethod.GET, "/api/books/{id}/availability", 200, 200, 200),
                new EndpointSecurityCase(HttpMethod.POST, "/api/books", 403, 403, 201),
                new EndpointSecurityCase(HttpMethod.PUT, "/api/books/{id}", 403, 403, 200),
                new EndpointSecurityCase(HttpMethod.PATCH, "/api/books/{id}", 403, 403, 200),
                new EndpointSecurityCase(HttpMethod.DELETE, "/api/books/{id}", 403, 403, 204),
                new EndpointSecurityCase(HttpMethod.POST, "/api/books/{id}/reserve", 403, 200, 200),
                new EndpointSecurityCase(HttpMethod.GET, "/api/users", 403, 403, 200),
                new EndpointSecurityCase(HttpMethod.GET, "/api/users/{id}", 403, 403, 200),
                new EndpointSecurityCase(HttpMethod.POST, "/api/users", 403, 403, 201),
                new EndpointSecurityCase(HttpMethod.PUT, "/api/users/{id}", 403, 403, 200),
                new EndpointSecurityCase(HttpMethod.PATCH, "/api/users/{id}", 403, 403, 200),
                new EndpointSecurityCase(HttpMethod.DELETE, "/api/users/{id}", 403, 403, 204)
        );
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("securityCases")
    void shouldApplySecurityRulesForAllEndpoints(EndpointSecurityCase c) throws Exception {
        setupAuthentication(USER_TOKEN, "USER");
        setupAuthentication(ADMIN_TOKEN, "ADMIN");

        perform(c, null)
                .andExpect(status().is(c.expectedWithoutToken()));

        perform(c, USER_TOKEN)
                .andExpect(status().is(c.expectedUser()));

        perform(c, ADMIN_TOKEN)
                .andExpect(status().is(c.expectedAdmin()));
    }

    private ResultActions perform(EndpointSecurityCase c, String token) throws Exception {
        String resolvedUrl = c.url().replace("{id}", UUID.randomUUID().toString());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(c.method(), resolvedUrl)
                .contentType(MediaType.APPLICATION_JSON);

        String json = requestBodyFor(c);
        if (json != null) {
            request.content(json);
        }

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return mockMvc.perform(request);
    }

    private String requestBodyFor(EndpointSecurityCase c) throws Exception {
        if (c.method() == HttpMethod.POST && "/auth/login".equals(c.url())) {
            return objectMapper.writeValueAsString(new LoginPayload("john.doe@test.com", "MySecurePassword123"));
        }

        if (c.url().startsWith("/api/books") && (c.method() == HttpMethod.POST || c.method() == HttpMethod.PUT)) {
            return objectMapper.writeValueAsString(new BookCreatePayload(
                    "Clean Code",
                    "Robert C. Martin",
                    "9780132350884",
                    2008,
                    "Programming",
                    5
            ));
        }

        if (c.url().startsWith("/api/books") && c.method() == HttpMethod.PATCH) {
            return objectMapper.writeValueAsString(new BookPatchPayload("Clean Code 2"));
        }

        if (c.url().startsWith("/api/users") && (c.method() == HttpMethod.POST || c.method() == HttpMethod.PUT)) {
            return objectMapper.writeValueAsString(new UserCreatePayload(
                    "John",
                    "Doe",
                    "john.doe@test.com",
                    "MySecurePassword123",
                    "USER"
            ));
        }

        if (c.url().startsWith("/api/users") && c.method() == HttpMethod.PATCH) {
            return objectMapper.writeValueAsString(new UserPatchPayload("Jane"));
        }

        return null;
    }

    private void setupAuthentication(String token, String roleName) {
        UUID userId = UUID.randomUUID();
        User user = User.restore(
                userId,
                "Test",
                "User",
                Email.of("test@library.com"),
                null,
                Role.builder().name(roleName).build());

        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(token, user)).thenReturn(true);
    }

    record LoginPayload(String email, String password) {}

    record BookCreatePayload(
            String title,
            String author,
            String isbn,
            int publishedYear,
            String genre,
            int totalCopies
    ) {}

    record BookPatchPayload(String title) {}

    record UserCreatePayload(
            String firstName,
            String lastName,
            String email,
            String password,
            String roleName
    ) {}

    record UserPatchPayload(String firstName) {}
}
