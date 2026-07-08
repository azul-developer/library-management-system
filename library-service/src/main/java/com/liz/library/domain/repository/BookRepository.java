package com.liz.library.domain.repository;

import com.liz.library.domain.model.Book;
import com.liz.library.domain.query.BookQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findById(UUID id);
    /**
     * Find books with optional filters and pagination (domain-level query).
     */
    Page<Book> findAllByQuery(BookQuery query, Pageable pageable);

    void delete(Book book);

    boolean existsByIsbn(String isbn);
}