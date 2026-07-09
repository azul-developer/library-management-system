package com.liz.library.application.service;

import com.liz.library.application.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookService {

    BookResponse create(CreateBookRequest request);

    /**
     * Retrieves books using optional filters (author, genre, availability) and supports pagination.
     */
    PageResponse<BookResponse> findAll(BookFilter filter, Pageable pageable);

    BookResponse findById(UUID id);

    BookResponse update(UUID id, CreateBookRequest request);

    BookResponse partialUpdate(UUID id, UpdateBookRequest request);

    void delete(UUID id);
}