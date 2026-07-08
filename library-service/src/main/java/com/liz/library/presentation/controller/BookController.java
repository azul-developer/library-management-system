package com.liz.library.presentation.controller;

import com.liz.library.application.dto.BookFilter;
import com.liz.library.application.dto.BookResponse;
import com.liz.library.application.dto.CreateBookRequest;
import com.liz.library.application.dto.UpdateBookRequest;
import com.liz.library.application.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody CreateBookRequest request) {
        return bookService.create(request);
    }

    @GetMapping
    public Page<BookResponse> list(
            @ModelAttribute BookFilter filter,
            Pageable pageable
    ) {
        return bookService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable UUID id) {
        return bookService.findById(id);
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable UUID id, @Valid @RequestBody CreateBookRequest request) {
        return bookService.update(id, request);
    }

    @PatchMapping("/{id}")
    public BookResponse patch(@PathVariable UUID id, @RequestBody UpdateBookRequest request) {
        return bookService.partialUpdate(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        bookService.delete(id);
    }

    @PostMapping("/{id}/reserve")
    @ResponseStatus(HttpStatus.OK)
    public void reserve(@PathVariable UUID id) {

    }
}