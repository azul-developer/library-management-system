package com.liz.library.application.service.impl;

import com.liz.library.application.dto.*;
import com.liz.library.application.factory.BookFactory;
import com.liz.library.application.mapper.BookMapper;
import com.liz.library.application.service.BookService;
import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import com.liz.library.domain.model.Book;
import com.liz.library.domain.query.BookQuery;
import com.liz.library.domain.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookFactory bookFactory;
    private final BookMapper bookMapper;

    private Book loadBook(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCodes.BOOK_NOT_FOUND));
    }

    private int calculateAvailableCopies(Book existing, int newTotalCopies) {

        int borrowedCopies = existing.getTotalCopies() - existing.getAvailableCopies();

        if (newTotalCopies < borrowedCopies) {
            throw new BusinessException(MessageCodes.INVALID_TOTAL_COPIES);
        }

        return newTotalCopies - borrowedCopies;
    }

    @Override
    public BookResponse create(CreateBookRequest request) {

        Book book = bookFactory.create(request);

        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException(MessageCodes.BOOK_ALREADY_EXISTS);
        }

        Book savedBook = bookRepository.save(book);

        return bookMapper.toResponse(savedBook);
    }

    @Override
    public Page<BookResponse> findAll(BookFilter filter, Pageable pageable) {

        BookQuery domainQuery = BookQuery.of(
                filter != null ? filter.getAuthor() : null,
                filter != null ? filter.getGenre() : null,
                filter != null ? filter.getAvailable() : null
        );

        Page<Book> books = bookRepository.findAllByQuery(domainQuery, pageable);

        return books.map(bookMapper::toResponse);
    }

    @Override
    public BookResponse findById(UUID id) {
        Book book = loadBook(id);
        return bookMapper.toResponse(book);
    }


    @Override
    public BookResponse update(UUID id, CreateBookRequest request) {

        Book existing = loadBook(id);

        int availableCopies = calculateAvailableCopies(existing, request.getTotalCopies());

        Book updated = Book.restore(
                existing.getId(),
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPublishedYear(),
                request.getGenre(),
                request.getTotalCopies(),
                availableCopies
        );

        Book saved = bookRepository.save(updated);

        return bookMapper.toResponse(saved);
    }


    @Override
    public BookResponse partialUpdate(UUID id, UpdateBookRequest request) {
        Book existing = loadBook(id);

        String title = request.getTitle() != null ? request.getTitle().trim() : existing.getTitle();
        String author = request.getAuthor() != null ? request.getAuthor().trim() : existing.getAuthor();
        String isbn = request.getIsbn() != null ? request.getIsbn().trim() : existing.getIsbn();
        Integer publishedYear = request.getPublishedYear() != null ? request.getPublishedYear() : existing.getPublishedYear();
        String genre = request.getGenre() != null ? request.getGenre().trim() : existing.getGenre();
        Integer totalCopies = request.getTotalCopies() != null ? request.getTotalCopies() : existing.getTotalCopies();

        int availableCopies = request.getTotalCopies() != null
                ? calculateAvailableCopies(existing, totalCopies)
                : existing.getAvailableCopies();

        Book updated = Book.restore(
                existing.getId(),
                title,
                author,
                isbn,
                publishedYear,
                genre,
                totalCopies,
                availableCopies
        );

        Book saved = bookRepository.save(updated);
        return bookMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Book book = loadBook(id);
        bookRepository.delete(book);
    }
}
