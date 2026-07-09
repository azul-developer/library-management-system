package com.liz.library.presentation.controller;

import com.liz.library.application.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @Test
    void delete_invokesServiceDelete() {
        UUID id = UUID.randomUUID();

        bookController.delete(id);

        verify(bookService, times(1)).delete(id);
        verifyNoMoreInteractions(bookService);
    }
}
