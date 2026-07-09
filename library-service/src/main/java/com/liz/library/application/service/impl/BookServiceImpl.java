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
import com.liz.library.infrastructure.client.LoanClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookFactory bookFactory;
    private final BookMapper bookMapper;
    private final LoanClient loanClient;

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
    public PageResponse<BookResponse> findAll(BookFilter filter, Pageable pageable) {

        BookQuery domainQuery = BookQuery.of(
                filter != null ? filter.getAuthor() : null,
                filter != null ? filter.getGenre() : null,
                filter != null ? filter.getAvailable() : null
        );

        Page<Book> books = bookRepository.findAllByQuery(domainQuery, pageable);

        List<BookResponse> content = books.getContent()
                .stream()
                .map(bookMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages()
        );
    }

    @Override
    public BookResponse findById(UUID id) {
        Book book = loadBook(id);
        return bookMapper.toResponse(book);
    }

    @Override
    public boolean isAvailable(UUID id) {
        Book book = loadBook(id);
        Integer copies = book.getAvailableCopies();
        return copies != null && copies > 0;
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

    @Override
    public LoanResponse createLoan(UUID userId, UUID bookId) {
        LoanResponse loanResponse = loanClient.createLoan(userId, bookId);
        if (!bookRepository.tryReserve(bookId)) {
            log.error(
                    "Loan {} was created, but inventory update failed for book {}. Manual reconciliation may be required.",
                    loanResponse.getId(),
                    bookId);

            // TODO: Implement a compensating action or a reconciliation process
            // to synchronize the inventory with the loan service.
        }

        return loanResponse;
    }
}
