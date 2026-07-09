package com.liz.library.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liz.library.application.dto.BookResponse;
import com.liz.library.application.dto.CreateBookRequest;
import com.liz.library.application.dto.LoanResponse;
import com.liz.library.application.dto.PageResponse;
import com.liz.library.application.dto.UpdateBookRequest;
import com.liz.library.application.service.BookService;
import com.liz.library.domain.model.Role;
import com.liz.library.domain.model.User;
import com.liz.library.domain.repository.UserRepository;
import com.liz.library.domain.valueobject.Email;
import com.liz.library.infrastructure.config.SecurityConfig;
import com.liz.library.infrastructure.security.JwtAuthenticationFilter;
import com.liz.library.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@org.springframework.context.annotation.Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class BookControllerAccessTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void adminCanCreateBook() throws Exception {
        CreateBookRequest request = validCreateBookRequest();
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        when(bookService.create(any())).thenReturn(sampleBookResponse());

        mockMvc.perform(post("/api/books")
                        .with(csrf())
                .header("Authorization", bearer(ADMIN_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(bookService).create(any());
    }

    @Test
    void adminCanUpdateBook() throws Exception {
        UUID bookId = UUID.randomUUID();
        UpdateBookRequest request = validUpdateBookRequest();
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        when(bookService.update(eq(bookId), any())).thenReturn(sampleBookResponse());

        mockMvc.perform(put("/api/books/{id}", bookId)
                        .with(csrf())
                .header("Authorization", bearer(ADMIN_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(bookService).update(eq(bookId), any());
    }

    @Test
    void adminCanDeleteBook() throws Exception {
        UUID bookId = UUID.randomUUID();
        setupAuthentication(ADMIN_TOKEN, UUID.randomUUID(), "ADMIN");
        doNothing().when(bookService).delete(bookId);

        mockMvc.perform(delete("/api/books/{id}", bookId)
                .with(csrf())
                .header("Authorization", bearer(ADMIN_TOKEN)))
                .andExpect(status().isNoContent());

        verify(bookService).delete(bookId);
    }

    @Test
    void userCanListBooks() throws Exception {
        setupAuthentication(USER_TOKEN, UUID.randomUUID(), "USER");
        when(bookService.findAll(any(), any())).thenReturn(
                new PageResponse<>(List.of(sampleBookResponse()), 0, 10, 1, 1));

        mockMvc.perform(get("/api/books")
                .header("Authorization", bearer(USER_TOKEN)))
                .andExpect(status().isOk());

        verify(bookService).findAll(any(), any());
    }

    @Test
    void userCanGetBook() throws Exception {
        UUID bookId = UUID.randomUUID();
        setupAuthentication(USER_TOKEN, UUID.randomUUID(), "USER");
        when(bookService.findById(bookId)).thenReturn(sampleBookResponse());

        mockMvc.perform(get("/api/books/{id}", bookId)
                .header("Authorization", bearer(USER_TOKEN)))
                .andExpect(status().isOk());

        verify(bookService).findById(bookId);
    }

    @Test
    void authenticatedUserCanReserveBook() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        setupAuthentication(USER_TOKEN, userId, "USER");
        when(bookService.createLoan(userId, bookId)).thenReturn(sampleLoanResponse(userId, bookId));

        mockMvc.perform(post("/api/books/{id}/reserve", bookId)
                        .with(csrf())
                .header("Authorization", bearer(USER_TOKEN)))
                .andExpect(status().isOk());

        verify(bookService).createLoan(userId, bookId);
    }

    @Test
    void userCannotCreateBook() throws Exception {
        setupAuthentication(USER_TOKEN, UUID.randomUUID(), "USER");
        mockMvc.perform(post("/api/books")
                        .with(csrf())
                .header("Authorization", bearer(USER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateBookRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotDeleteBook() throws Exception {
        setupAuthentication(USER_TOKEN, UUID.randomUUID(), "USER");
        mockMvc.perform(delete("/api/books/{id}", UUID.randomUUID())
                .with(csrf())
                .header("Authorization", bearer(USER_TOKEN)))
                .andExpect(status().isForbidden());
    }

        private void setupAuthentication(String token, UUID userId, String roleName) {
        User user = User.restore(
            userId,
            "Test",
            "User",
            Email.of("test@library.com"),
            null,
            Role.builder().name(roleName).build());

        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(jwtService.isTokenValid(token, user)).thenReturn(true);
        }

        private String bearer(String token) {
        return "Bearer " + token;
        }

    private CreateBookRequest validCreateBookRequest() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setIsbn("9780132350884");
        request.setPublishedYear(2008);
        request.setGenre("Programming");
        request.setTotalCopies(5);
        return request;
    }

    private UpdateBookRequest validUpdateBookRequest() {
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Clean Code 2");
        request.setAuthor("Robert C. Martin");
        request.setIsbn("9780132350884");
        request.setPublishedYear(2009);
        request.setGenre("Programming");
        request.setTotalCopies(4);
        return request;
    }

    private BookResponse sampleBookResponse() {
        return BookResponse.builder()
                .id(UUID.randomUUID())
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .publishedYear(2008)
                .genre("Programming")
                .totalCopies(5)
                .availableCopies(5)
                .build();
    }

    private LoanResponse sampleLoanResponse(UUID userId, UUID bookId) {
        return LoanResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .bookId(bookId)
                .createdAt(OffsetDateTime.now())
                .returned(false)
                .build();
    }
}
