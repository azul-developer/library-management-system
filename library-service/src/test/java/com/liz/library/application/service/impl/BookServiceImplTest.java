package com.liz.library.application.service.impl;

import com.liz.library.application.dto.LoanResponse;
import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import com.liz.library.domain.repository.BookRepository;
import com.liz.library.infrastructure.client.LoanClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanClient loanClient;

    @InjectMocks
    private BookServiceImpl bookService;

    private UUID userId;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
    }

    @Test
    void shouldCreateLoanSuccessfully() {

        // Arrange
        LoanResponse expected = LoanResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .bookId(bookId)
                .createdAt(OffsetDateTime.now())
                .returned(false)
                .build();

        when(loanClient.createLoan(userId, bookId))
                .thenReturn(expected);

        when(bookRepository.tryReserve(bookId))
                .thenReturn(true);

        // Act
        LoanResponse result = bookService.createLoan(userId, bookId);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getUserId(), result.getUserId());
        assertEquals(expected.getBookId(), result.getBookId());
        assertEquals(expected.getReturned(), result.getReturned());

        verify(loanClient).createLoan(userId, bookId);
        verify(bookRepository).tryReserve(bookId);

        verifyNoMoreInteractions(bookRepository, loanClient);
    }

    @Test
    void shouldThrowBookNotFoundWhenBookDoesNotExist() {

        // Arrange
        when(loanClient.createLoan(userId, bookId))
                .thenThrow(new BusinessException(MessageCodes.BOOK_NOT_FOUND));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookService.createLoan(userId, bookId)
        );

        assertEquals(MessageCodes.BOOK_NOT_FOUND, exception.getCode());

        verify(loanClient).createLoan(userId, bookId);

        verify(bookRepository, never()).tryReserve(any(UUID.class));

        verifyNoMoreInteractions(bookRepository, loanClient);
    }

    @Test
    void shouldPropagateBookNotAvailableWhenLoanServiceRejectsLoan() {

        // Arrange
        when(loanClient.createLoan(userId, bookId))
                .thenThrow(new BusinessException(MessageCodes.BOOK_NOT_AVAILABLE));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookService.createLoan(userId, bookId)
        );

        assertEquals(MessageCodes.BOOK_NOT_AVAILABLE, exception.getCode());

        verify(loanClient).createLoan(userId, bookId);

        // Como el préstamo nunca se creó, no debe actualizar el inventario.
        verify(bookRepository, never()).tryReserve(any(UUID.class));

        verifyNoMoreInteractions(bookRepository, loanClient);
    }

    @Test
    void shouldReturnLoanEvenWhenInventoryUpdateFails() {

        // Arrange
        LoanResponse expected = LoanResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .bookId(bookId)
                .createdAt(OffsetDateTime.now())
                .returned(false)
                .build();

        when(loanClient.createLoan(userId, bookId))
                .thenReturn(expected);

        // Simulate failure updating the inventory
        when(bookRepository.tryReserve(bookId))
                .thenReturn(false);

        // Act
        LoanResponse result = bookService.createLoan(userId, bookId);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getUserId(), result.getUserId());
        assertEquals(expected.getBookId(), result.getBookId());

        verify(loanClient).createLoan(userId, bookId);
        verify(bookRepository).tryReserve(bookId);

        verifyNoMoreInteractions(bookRepository, loanClient);
    }
}