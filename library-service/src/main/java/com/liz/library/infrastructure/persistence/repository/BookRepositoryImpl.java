package com.liz.library.infrastructure.persistence.repository;

import com.liz.library.domain.model.Book;
import com.liz.library.domain.query.BookQuery;
import com.liz.library.domain.repository.BookRepository;
import com.liz.library.infrastructure.persistence.entity.BookEntity;
import com.liz.library.infrastructure.persistence.mapper.BookEntityMapper;
import com.liz.library.infrastructure.persistence.repository.specification.BookSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

    private final BookJpaRepository bookJpaRepository;

    @Override
    public Book save(Book book) {

        BookEntity entity = BookEntityMapper.toEntity(book);

        // If an entity with the same id is already managed by the persistence
        // context, update its fields instead of persisting a new instance to
        // avoid NonUniqueObjectException.
        return bookJpaRepository.findById(entity.getId())
                .map(managed -> {
                    managed.setTitle(entity.getTitle());
                    managed.setAuthor(entity.getAuthor());
                    managed.setIsbn(entity.getIsbn());
                    managed.setPublishedYear(entity.getPublishedYear());
                    managed.setGenre(entity.getGenre());
                    managed.setTotalCopies(entity.getTotalCopies());
                    managed.setAvailableCopies(entity.getAvailableCopies());
                    BookEntity saved = bookJpaRepository.save(managed);
                    return BookEntityMapper.toDomain(saved);
                })
                .orElseGet(() -> {
                    BookEntity saved = bookJpaRepository.save(entity);
                    return BookEntityMapper.toDomain(saved);
                });
    }

    @Override
    public Optional<Book> findById(UUID id) {

        return bookJpaRepository.findById(id)
                .map(BookEntityMapper::toDomain);
    }

    @Override
    public Page<Book> findAllByQuery(BookQuery query, Pageable pageable) {

        String author = query != null ? query.getAuthor() : null;
        String genre = query != null ? query.getGenre() : null;
        Boolean available = query != null ? query.getAvailable() : null;

        Specification<BookEntity> spec = BookSpecifications.byFilters(author, genre, available);

        return bookJpaRepository.findAll(spec, pageable)
                .map(BookEntityMapper::toDomain);
    }

    @Override
    public void delete(Book book) {
        // Use deleteById to avoid passing an entity without the version
        // (domain model doesn't carry the JPA `version` field) which
        // could lead to optimistic locking mismatches when removing.
        bookJpaRepository.deleteById(book.getId());
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return bookJpaRepository.existsByIsbn(isbn);
    }
}